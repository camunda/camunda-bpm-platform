define([
  'text!./cam-tasklist-task.html'
], function(template) {
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
      function(
        $scope,
        $q,
        camAPI,
        Views
      ) {

        var History = camAPI.resource('history');
        var Task = camAPI.resource('task');

        var taskData = $scope.taskData = $scope.tasklistData.newChild($scope);

        taskData.provide('assignee', ['task', function(task) {
          if (task) {
            return task.assignee;
          }

          return null;
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

        /**
         * expose current task as scope variable
         */
        taskData.observe('task', function(task) {
          $scope.task = task;
        });

        // plugins //////////////////////////////////////////////////////////////

        $scope.taskVars = { read: [ 'task', 'taskData' ] };
        $scope.taskDetailTabs = Views.getProviders({ component: 'tasklist.task.detail' });

        $scope.selectedTaskDetailTab = $scope.taskDetailTabs[0];

        $scope.selectTaskDetailTab = function(tab) {
          $scope.selectedTaskDetailTab = tab;
        };

      }]
    };
  }];
});

