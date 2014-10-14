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
        '$location',
        '$q',
        'dataDepend',
        'camAPI',
      function(
        $scope,
        $location,
        $q,
        dataDepend,
        camAPI
      ) {

        if (!$scope.tasklistData) {
          return;
        }

        var taskData = $scope.taskData = $scope.tasklistData.newChild($scope);

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

