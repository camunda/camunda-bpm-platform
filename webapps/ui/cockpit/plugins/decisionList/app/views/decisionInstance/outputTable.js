'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/output-variable-table.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.decisionInstance.tab', {
    id: 'decision-input-table',
    label: 'PLUGIN_OUTPUT_TABLE_LABEL',
    template: template,
    controller: [
      '$scope',
      '$translate',
      'localConf',
      'orderByFilter',
      function($scope, $translate, localConf, orderBy) {

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

        $scope.headColumns = [
          { class: 'name',  request: 'variable.name', sortable: true, content: $translate.instant('PLUGIN_VARIABLE_NAME')},
          { class: 'type',  request: 'variable.type', sortable: true, content: $translate.instant('PLUGIN_VARIABLE_TYPE')},
          { class: 'value', request: '', sortable: false, content: $translate.instant('PLUGIN_VARIABLE_VALUE')}
        ];

        // Default sorting
        $scope.sortObj   = loadLocal({ sortBy: 'variable.name', sortOrder: 'asc', sortReverse: false });

        $scope.onSortChange = function(sortObj) {
          sortObj = sortObj || $scope.sortObj;
          sortObj.sortReverse = sortObj.sortOrder !== 'asc';
          saveLocal(sortObj);
          // Angular filter function
          $scope.variables = orderBy($scope.variables, sortObj.sortBy, sortObj.sortReverse);
        };

        function saveLocal(sortObj) {
          localConf.set('sortDecisionInputTab', sortObj);

        }

        function loadLocal(defaultValue) {
          return localConf.get('sortDecisionInputTab', defaultValue);
        }

      }],
    priority: 10
  });
}];
