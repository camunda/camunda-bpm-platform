  'use strict';

  module.exports = [
    '$scope',
    'Views',
    function(
    $scope,
    Views
  ) {

      $scope.deploymentsData = $scope.repositoryData.newChild($scope);
      $scope.deploymentsVars = { read: [ 'deploymentsData' ] };
      $scope.deploymentsPlugins = Views.getProviders({ component: 'cockpit.repository.deployments.list' });

    }];
