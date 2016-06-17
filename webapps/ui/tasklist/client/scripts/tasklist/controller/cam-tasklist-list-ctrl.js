  'use strict';

  module.exports = [
    '$scope',
    'Views',
    function(
    $scope,
    Views
  ) {

      $scope.tasklistVars = { read: [ 'tasklistData' ] };
      $scope.tasklistPlugins = Views.getProviders({ component: 'tasklist.list' });

    }];
