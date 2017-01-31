'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-task.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [ function() {

  return {
    restrict: 'A',
    scope: {
      tasklistData: '='
    },

    template: template,

    controller : [
      '$scope',
      '$q',
      '$location',
      '$translate',
      // not using $timeout here (although it would be better) because... well... i'd rather not give my opinion
      // https://github.com/angular/protractor/blob/master/docs/timeouts.md#waiting-for-angular
      '$interval',
      'camAPI',
      'Notifications',
      'Views',
      'search',
      function(
        $scope,
        $q,
        $location,
        $translate,
        $interval,
        camAPI,
        Notifications,
        Views,
        search
      ) {

        // setup /////////////////////////////////////////////////////////////////////

        var taskData = $scope.taskData = $scope.tasklistData.newChild($scope);

        // error handling //////////////////////////////////////////////////////////////

        function errorNotification(src, err) {
          $translate(src).then(function(translated) {
            Notifications.addError({
              status: translated,
              message: (err ? err.message : ''),
              exclusive: true,
              scope: $scope
            });
          });
        }

        $scope.errorHandler = function(status, err) {
          var _status = enhanceErrorMessage(err.message);

          if(_status === 'TASK_NOT_EXIST' || _status === 'INSTANCE_SUSPENDED') {
            return $translate(_status).then(function(translated) {
              err.message = translated;
              errorNotification(status, err);
              clearTask(true);
            });

          }
          else {
            errorNotification(status, err);
          }

        };

        function enhanceErrorMessage(msg) {
          if (msg) {
            if(msg.indexOf('task is null') !== -1 || msg.indexOf('No matching task') !== -1) {
              // task does not exist (e.g. completed by someone else)
              return 'TASK_NOT_EXIST';
            }
            else if(msg.indexOf('is suspended') !== -1) {
              // process instance is suspended
              return 'INSTANCE_SUSPENDED';
            }
          }
          return msg;
        }

        function clearTask(updateLocation) {

          if (updateLocation) {
            var search = $location.search() || {};

            delete search.task;
            delete search.detailsTab;

            // reseting the location leads that
            // the taskId will set to null and
            // the current selected task will
            // also be set to null, so that the
            // view gets clear
            $location.search(angular.copy(search));

          }
          else {
            // reset current select taskId to null
            taskData.set('taskId', { 'taskId' : null });
          }

          // list of tasks must be reloaded as
          // well: changed properties on this
          // task may cause the list to change
          taskData.changed('taskList');
        }

        $scope.$watch('taskState.$error', function(err) {
          if (err) {
            var src = enhanceErrorMessage(err.message);
            errorNotification(src, err);
            // pass false to not reset the search params!
            // in that case the history works properly.
            clearTask(false);
          }
        });

        // provider ///////////////////////////////////////////////////////////////////

        taskData.provide('assignee', ['task', function(task) {
          if (task && task._embedded) {
            if(task._embedded.identityLink) {
              for(var i = 0; i < task._embedded.identityLink.length; i++) {
                if(task._embedded.identityLink[i].type === 'assignee') {
                  if(task._embedded.identityLink[i]._embedded.user) {
                    return task._embedded.identityLink[i]._embedded.user[0];
                  } else {
                    return {id: task._embedded.identityLink[i].userId};
                  }

                }
              }
            }
          }
          return null;
        }]);

        taskData.provide('groups', ['task', function(task) {
          var groups = [];
          if (task && task._embedded) {
            if(task._embedded.identityLink) {
              for(var i = 0; i < task._embedded.identityLink.length; i++) {
                if(task._embedded.identityLink[i].type === 'candidate' && task._embedded.identityLink[i].groupId !== null) {
                  if(task._embedded.identityLink[i]._embedded.group) {
                    groups.push(task._embedded.identityLink[i]._embedded.group[0]);
                  } else {
                    groups.push({id: task._embedded.identityLink[i].groupId});
                  }
                }
              }
            }
          }
          return groups;
        }]);

        taskData.provide('isAssignee', ['assignee', function(assignee) {
          return !!assignee && assignee.id === $scope.$root.authentication.name;
        }]);

        taskData.provide('processDefinition', ['task', function(task) {
          if (!task || !task._embedded || !task._embedded.processDefinition) {
            return null;
          }
          return task._embedded.processDefinition[0];
        }]);

        taskData.provide('caseDefinition', ['task', function(task) {
          if (!task || !task._embedded || !task._embedded.caseDefinition) {
            return null;
          }
          return task._embedded.caseDefinition[0];
        }]);

        // observer ////////////////////////////////////////////////////////////////////////

        /**
         * expose current task as scope variable
         */
        $scope.taskState = taskData.observe('task', function(task) {
          $scope.task = task;
        });

        taskData.observe('isAssignee', function(isAssignee) {
          $scope.isAssignee = isAssignee;
        });

        // plugins //////////////////////////////////////////////////////////////

        $scope.taskVars = { read: [ 'task', 'taskData', 'errorHandler' ] };
        $scope.taskDetailTabs = Views.getProviders({ component: 'tasklist.task.detail' });

        $scope.selectedTaskDetailTab = $scope.taskDetailTabs[0];

        $scope.selectTaskDetailTab = function(tab) {
          if (!$scope.taskExists) return;
          $scope.selectedTaskDetailTab = tab;

          search.updateSilently({
            detailsTab: tab.id
          });
        };

        function setDefaultTaskDetailTab(tabs) {
          var selectedTabId = search().detailsTab;

          if (!tabs || !tabs.length) {
            return;
          }

          if (selectedTabId) {
            var provider = Views.getProvider({ component: 'tasklist.task.detail', id: selectedTabId });
            if (provider && tabs.indexOf(provider) != -1) {
              $scope.selectedTaskDetailTab = provider;
              return;
            }
          }

          search.updateSilently({
            detailsTab: null
          });

          $scope.selectedTaskDetailTab = tabs[0];
        }

        setDefaultTaskDetailTab($scope.taskDetailTabs);

        $scope.$on('$routeChanged', function() {
          setDefaultTaskDetailTab($scope.taskDetailTabs);
        });






        // handling removed task

        var taskResource = camAPI.resource('task');

        $scope.taskExists = false;
        $scope.$watch('task.id', function(newVal) {
          $scope.taskExists = !!newVal;
        });

        $scope.dismissTask = function() {
          clearTask(true);
        };

        $scope.$on('refresh', function() {
          if (!$scope.task || !$scope.taskExists) return;

          taskResource.get($scope.task.id, function(err) {
            if (err) {
              $scope.taskExists = false;
              $scope.$broadcast('taskremoved');
            }
          });
        });
      }]
  };
}];
