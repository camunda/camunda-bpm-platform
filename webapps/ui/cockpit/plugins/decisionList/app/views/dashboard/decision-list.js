'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decision-list.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.decisions.dashboard', {
    id: 'decision-list',
    label: 'Deployed Decision Tables',
    template: template,
    controller: [
      '$scope', 'camAPI',
      function($scope,   camAPI) {
        $scope.loadingState = 'LOADING';

        var decisionDefinitionService = camAPI.resource('decision-definition');

        // get ALL the decisions
        decisionDefinitionService.list({
          latestVersion: true,
          sortBy: 'name',
          sortOrder: 'asc'
        }, function(err, data) {
          if (err) {
            $scope.loadingError = err.message;
            $scope.loadingState = 'ERROR';
            throw err;
          }
          $scope.loadingState = 'LOADED';
          $scope.decisionCount = data.length;
          $scope.decisions = data;
        });

      }],

    priority: -5 // display below the process definition list
  });
}];
