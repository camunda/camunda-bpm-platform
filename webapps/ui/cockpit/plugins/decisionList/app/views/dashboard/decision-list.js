'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decision-list.html', 'utf8');

  module.exports = [ 'ViewsProvider', function (ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'decision-list',
      label: 'Deployed Decision Tables',
      dashboardMenuLabel: 'DMN Decisions',
      template: template,
      controller: [
              '$scope', 'camAPI',
      function($scope,   camAPI) {

        var decisionDefinitionService = camAPI.resource('decision-definition');

        $scope.orderByPredicate = 'name';
        $scope.orderByReverse = false;

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
