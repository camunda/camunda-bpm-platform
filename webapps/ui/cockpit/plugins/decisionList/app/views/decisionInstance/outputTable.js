'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/output-variable-table.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.decisionInstance.tab', {
    id: 'decision-input-table',
    label: 'Outputs',
    template: template,
    controller: [
      '$scope',
      function($scope) {

        $scope.variables = $scope.decisionInstance.outputs.map(function(variable) {
          return {
            variable: {
              type: variable.type,
              value: variable.value,
              name: variable.clauseName || variable.clauseId || variable.variableName,
              valueInfo: variable.valueInfo
            }
          };
        });
      }],
    priority: 10
  });
}];
