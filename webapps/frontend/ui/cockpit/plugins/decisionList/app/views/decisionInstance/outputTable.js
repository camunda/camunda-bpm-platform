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

var template = require('./output-variable-table.html?raw');

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
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
          $scope.variables = $scope.decisionInstance.outputs.map(function(
            variable
          ) {
            const variableValue =
              variable.type === 'Date'
                ? new Date(variable.value)
                : variable.value;
            return {
              variable: {
                type: variable.type,
                value: variableValue,
                name:
                  variable.clauseName ||
                  variable.clauseId ||
                  variable.variableName,
                valueInfo: variable.valueInfo
              }
            };
          });

          // prettier-ignore
          $scope.headColumns = [
          { class: 'name',  request: 'variable.name', sortable: true, content: $translate.instant('PLUGIN_VARIABLE_NAME')},
          { class: 'type',  request: 'variable.type', sortable: true, content: $translate.instant('PLUGIN_VARIABLE_TYPE')},
          { class: 'value', request: '', sortable: false, content: $translate.instant('PLUGIN_VARIABLE_VALUE')}
        ];

          // Default sorting
          $scope.sortObj = loadLocal({
            sortBy: 'variable.name',
            sortOrder: 'asc',
            sortReverse: false
          });

          $scope.onSortChange = function(sortObj) {
            sortObj = sortObj || $scope.sortObj;
            sortObj.sortReverse = sortObj.sortOrder !== 'asc';
            saveLocal(sortObj);
            // Angular filter function
            $scope.variables = orderBy(
              $scope.variables,
              sortObj.sortBy,
              sortObj.sortReverse
            );
          };

          function saveLocal(sortObj) {
            localConf.set('sortDecisionInputTab', sortObj);
          }

          function loadLocal(defaultValue) {
            return localConf.get('sortDecisionInputTab', defaultValue);
          }
        }
      ],
      priority: 10
    });
  }
];
