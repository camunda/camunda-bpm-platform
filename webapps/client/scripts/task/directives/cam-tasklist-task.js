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
      function(
        $scope
      ) {

        if (!$scope.tasklistData) {
          return;
        }

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

        /**
         * expose current task as scope variable
         */
        taskData.observe('task', function(task) {
          $scope.task = task;
        });

      }]
    };
  }];
});

