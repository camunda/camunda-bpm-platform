'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-deployment.html', 'utf8');

module.exports = [function() {

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
      function(
        $scope,
        Views
      ) {

        $scope.deploymentData = $scope.deploymentsListData.newChild($scope);
        $scope.deploymentVars = { read: [ 'deploymentData', 'deployment', 'control' ] };
        $scope.deploymentPlugins = Views.getProviders({ component: 'cockpit.repository.deployment.action' });

      }]
  };
}];
