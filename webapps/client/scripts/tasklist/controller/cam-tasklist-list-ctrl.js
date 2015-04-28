define([], function() {
  'use strict';

  return [
    '$scope',
    'Views',
  function(
    $scope,
    Views
  ) {

    $scope.tasklistVars = { read: [ 'tasklistData' ] };
    $scope.tasklistPlugins = Views.getProviders({ component: 'tasklist.list' });

  }];

});
