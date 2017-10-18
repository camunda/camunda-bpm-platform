'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/input-variable-table.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.decisionInstance.tab', {
    id: 'decision-input-table',
    label: 'PLUGIN_INPUT_TABLE_LABEL',
    template: template,
    controller: [
      '$scope',
      function($scope) {
        $scope.loadingState = $scope.decisionInstance.inputs.length > 0 ? 'LOADED' : 'EMPTY';

        $scope.variables = $scope.decisionInstance.inputs.map(function(variable) {
          return {
            variable: {
              type: variable.type,
              value: variable.value,
              name: variable.clauseName || variable.clauseId,
              valueInfo: variable.valueInfo
            }
          };
        });
      }],
    priority: 20
  });
}];

