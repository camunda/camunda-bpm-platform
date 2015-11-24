define([
  'text!./decision-list.html'
], function(
  template
) {
  'use strict';

  return [ 'ViewsProvider', function (ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'decision-list',
      label: 'Deployed Decision Tables',
      dashboardMenuLabel: 'DMN Decisions',
      template: template,
      controller: [
              '$scope', 'camAPI',
      function($scope,   camAPI) {

        var decisionDefinitionService = camAPI.resource('decision-definition');

        // get ALL the decisions
        decisionDefinitionService.list({
          latestVersion: true,
          sortBy: 'name',
          sortOrder: 'asc'
        }, function(err, data) {
          $scope.decisionCount = data.length;
          $scope.decisions = data;
        });

      }],

      priority: -5 // display below the process definition list
    });
  }];
});
