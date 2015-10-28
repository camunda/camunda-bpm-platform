define([
  'angular',
  'text!./cam-cockpit-deployment.html'
], function(
  angular,
  template
) {
  'use strict';

  return [function() {

    return {

      restrict: 'A',
      scope: {
        deploymentsListData: '=',
        deployment: '=',
        control: '='
      },

      template: template,

      controller: [
        '$scope',
        'Views',
      function (
        $scope,
        Views
      ) {

        $scope.deploymentData = $scope.deploymentsListData.newChild($scope);
        $scope.deploymentVars = { read: [ 'deploymentData', 'deployment', 'control' ] };
        $scope.deploymentPlugins = Views.getProviders({ component: 'cockpit.repository.deployment.action' });

      }]
    };
  }];
});
