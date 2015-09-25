define([], function() {
  'use strict';

  return [
    '$scope',
    'Views',
  function(
    $scope,
    Views
  ) {

    $scope.deploymentsData = $scope.repositoryData.newChild($scope);
    $scope.deploymentsVars = { read: [ 'deploymentsData' ] };
    $scope.deploymentsPlugins = Views.getProviders({ component: 'cam.cockpit.repository.deployments.list' });

  }];

});
