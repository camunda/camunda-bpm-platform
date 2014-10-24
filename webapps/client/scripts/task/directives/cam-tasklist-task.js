define([
  'angular',
  'text!./cam-tasklist-task.html',
  'jquery'
], function(
  angular,
  template,
  jquery
) {
  'use strict';

  var TaskErrorManager = (function() {

    function TaskErrorManager() {

      this.errorProvider = null;

    }

    return TaskErrorManager;

  })();

  return [ function() {

    return {
      restrict: 'EAC',
      scope: {
        tasklistData: '='
      },

      template: template,

      controller : [
        '$scope',
        '$q',
        '$location',
        '$translate',
        'Notifications',
        'camAPI',
        'Views',
        'search',
      function(
        $scope,
        $q,
        $location,
        $translate,
        Notifications,
        camAPI,
        Views,
        search
      ) {

        // setup /////////////////////////////////////////////////////////////////////

        var History = camAPI.resource('history');
        var Task = camAPI.resource('task');

        var taskData = $scope.taskData = $scope.tasklistData.newChild($scope);

        // error handling //////////////////////////////////////////////////////////////

        function errorNotification(src, err) {
          $translate(src).then(function(translated) {
            Notifications.addError({
              duration: 3000,
              status: translated,
              message: (err ? err.message : ''),
              exclusive: true
            });
          });
        }

        function successNotification(src) {
          $translate(src).then(function(translated) {
            Notifications.addMessage({
              duration: 3000,
              status: translated
            });
          });
        }

        $scope.errorHandler = function (status, err) {
          var _status = enhanceErrorMessage(err.message);

          return $translate(_status).then(function(translated) {
            err.message = translated;

            errorNotification(status, err);

            if(_status === 'TASK_NOT_EXIST' || _status === 'INSTANCE_SUSPENDED') {
              clearTask(true);
            }

          });

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

        $scope.$watch('taskState.$error', function (err) {
          if (err) {
            var src = enhanceErrorMessage(err.message);
            errorNotification(src, err);
            // pass false to not reset the search params!
            // in that case the history works properly.
            clearTask(false);
          }
        });

        // handle successfully actions //////////////////////////////////////////////////

        $scope.successHandler = function (status) {
          successNotification(status);
        };

        // provider ///////////////////////////////////////////////////////////////////

        taskData.provide('assignee', ['task', function(task) {
          if (task) {
            return task.assignee;
          }

          return null;
        }]);

        taskData.provide('groups', ['task', function(task) {
          var deferred = $q.defer();

          if (!task) {
            return deferred.resolve(null);
          }
          Task.identityLinks(task.id, function(err, res) {
            if(err) {
              deferred.reject(err);
            }
            else {
              var groups = jquery.grep(res, function(identityLink) {
                return identityLink.groupId;
              }).map(function(groupObj) {
                return groupObj.groupId;
              });
              deferred.resolve(groups);
            }
          });

          return deferred.promise;
        }]);

        taskData.provide('isAssignee', ['assignee', function(assignee) {
          return assignee === $scope.$root.authentication.name;
        }]);

        taskData.provide('processDefinition', ['task', function (task) {
          if (!task) {
            return null;
          }

          return task._embedded.processDefinition[0];

        }]);

        taskData.provide('taskForm', ['task', function(task) {
          var deferred = $q.defer();

          if (!task || !task.id) {
            return deferred.resolve(null);
          }

          Task.form(task.id, function(err, res) {

            if(err) {
              deferred.reject(err);
            }
            else {
              deferred.resolve(res);
            }
          });

          return deferred.promise;
        }]);

        // observer ////////////////////////////////////////////////////////////////////////

        /**
         * expose current task as scope variable
         */
        $scope.taskState = taskData.observe('task', function(task) {
          $scope.task = task;
        });

        taskData.observe('isAssignee', function (isAssignee) {
          $scope.isAssignee = isAssignee;
        });

        // plugins //////////////////////////////////////////////////////////////

        $scope.taskVars = { read: [ 'task', 'taskData', 'errorHandler', 'successHandler' ] };
        $scope.taskDetailTabs = Views.getProviders({ component: 'tasklist.task.detail' });

        $scope.selectedTaskDetailTab = $scope.taskDetailTabs[0];

        $scope.selectTaskDetailTab = function(tab) {
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

      }]
    };
  }];
});

