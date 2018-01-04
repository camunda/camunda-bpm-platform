'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decisions-table.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    scope: {
      decisionCount: '=',
      decisions: '=',
      isDrdAvailable: '='
    },
    controller: ['$scope', 'localConf','$translate',
      function($scope, localConf, $translate) {

        $scope.headColumns = [
          { class: 'name',     request: 'key'          , sortable: true, content: $translate.instant('PLUGIN_DECISION_TABLE_NAME')},
          { class: 'tenant-id', request: 'tenantId'     , sortable: true, content: $translate.instant('PLUGIN_DECISION_TABLE_TENANT_ID')},
          { class: 'tenant-id',  request: 'drd.key', sortable: true, content: $translate.instant('PLUGIN_DECISION_TABLE_DECISION_REQUIREMENTS'), condition: $scope.isDrdAvailable}
        ];

        // Default sorting
        var defaultValue = { sortBy: 'key', sortOrder: 'asc', sortReverse: false};
        $scope.sortObj   = loadLocal(defaultValue);

        // Update Table
        $scope.onSortChange = function(sortObj) {
          sortObj = sortObj || $scope.sortObj;
          // transforms sortOrder to boolean required by anqular-sorting;
          sortObj.sortReverse = sortObj.sortOrder !== 'asc';
          saveLocal(sortObj);
          $scope.sortObj = sortObj;
        };

        function saveLocal(sortObj) {
          localConf.set('sortDecDefTab', sortObj);

        }
        function loadLocal(defaultValue) {
          return localConf.get('sortDecDefTab', defaultValue);
        }



      }]
  };
};
