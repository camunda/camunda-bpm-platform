/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var template = require('./decisions-table.html?raw');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    scope: {
      decisionCount: '=',
      decisions: '=',
      isDrdAvailable: '=',
      pagination: '='
    },
    controller: [
      '$scope',
      'localConf',
      '$translate',
      function($scope, localConf, $translate) {
        // prettier-ignore
        $scope.headColumns = [
          {class: 'name', request: 'name', sortable: true, content: $translate.instant('PLUGIN_DECISION_TABLE_NAME')},
          {class: 'tenant-id', request: 'tenantId', sortable: true, content: $translate.instant('PLUGIN_DECISION_TABLE_TENANT_ID')},
          {class: 'drd', request: 'decisionRequirementsDefinitionKey', sortable: true, content: $translate.instant('PLUGIN_DECISION_TABLE_DECISION_REQUIREMENTS'), condition: $scope.isDrdAvailable}
        ];

        // Default sorting
        var defaultValue = {
          sortBy: 'name',
          sortOrder: 'asc'
        };
        $scope.sortObj = loadLocal(defaultValue);

        // Update Table
        $scope.onSortChange = function(sortObj) {
          sortObj = sortObj || $scope.sortObj;
          $scope.sortObj = sortObj;

          $scope.pagination.changeDecisionSorting(sortObj);
        };

        function loadLocal(defaultValue) {
          return localConf.get('sortDecDefTable', defaultValue);
        }
      }
    ]
  };
};
