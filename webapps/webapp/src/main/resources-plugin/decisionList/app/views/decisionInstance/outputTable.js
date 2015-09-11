/* global define: false, angular: false */
define([
  'angular',
  'text!./variable-table.html'
],
function(angular, template) {
  'use strict';

  return [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionInstance.tab', {
      id: 'decision-input-table',
      label: 'Outputs',
      template: template,
      controller: [
               '$scope',
      function ($scope) {

        $scope.variables = $scope.decisionInstance.outputs;

        // the variable widget expects the variable name in the name field, not in the clauseName field
        for(var i = 0; i < $scope.variables.length; i++) {
          $scope.variables[i].name = $scope.variables[i].clauseName;
        }

      }],
      priority: 10
    });
  }];
});
