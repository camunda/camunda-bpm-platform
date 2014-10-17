define([
], function(
) {
  'use strict';

  return [
    '$scope',
    'Views',
  function(
    $scope,
    Views
  ) {

    var taskData = $scope.taskData = $scope.tasklistData.newChild($scope);

    /**
     * expose current task as scope variable
     */
    taskData.observe('task', function(task) {
      $scope.task = task;
    });

    // plugins //////////////////////////////////////////////////////////////

    $scope.taskVars = { read: [ 'task', 'taskData' ] };
    $scope.taskActions = Views.getProviders({ component: 'tasklist.task.action' });

  }];

});
