  'use strict';

  module.exports = [
    '$scope',
    'Views',
    'CamForm',
    function(
    $scope,
    Views,
    CamForm
  ) {

      var taskData = $scope.taskData = $scope.tasklistData.newChild($scope);

    /**
     * expose current task as scope variable
     */
      taskData.observe('task', function(task) {
        $scope.task = task;
      });

    /**
     * remove outdated saved forms
     */
      CamForm.cleanLocalStorage(Date.now() - 7 * 24 * 60 * 60 * 1000);

    // plugins //////////////////////////////////////////////////////////////

      $scope.taskVars = { read: [ 'task', 'taskData' ] };
      $scope.taskActions = Views.getProviders({ component: 'tasklist.task.action' });

    }];
