  'use strict';

  module.exports = [
    '$scope',
    'Views',
    function(
    $scope,
    Views
  ) {

      $scope.deploymentsData = $scope.repositoryData.newChild($scope);
      $scope.deploymentsVars = { read: [ 'deploymentsData', 'totalDeployments' ] };
      $scope.deploymentsPlugins = Views.getProviders({ component: 'cockpit.repository.deployments.list' });

    }];
