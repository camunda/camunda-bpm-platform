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
        'camAPI',
        'Views',
        'search',
      function(
        $scope,
        $q,
        camAPI,
        Views,
        search
      ) {

        // setup /////////////////////////////////////////////////////////////////////

        var History = camAPI.resource('history');
        var Task = camAPI.resource('task');

        var taskData = $scope.taskData = $scope.tasklistData.newChild($scope);

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

        $scope.taskVars = { read: [ 'task', 'taskData' ] };
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

