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

var fs = require('fs');

var template = require('./process-definitions.html')();

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processes.dashboard', {
      id: 'process-definition',
      label: 'Deployed Process Definitions',
      template: template,
      controller: [
        '$scope',
        'Views',
        'camAPI',
        'localConf',
        '$translate',
        function($scope, Views, camAPI, localConf, $translate) {
          var processDefinitionService = camAPI.resource('process-definition');

          $scope.processesActions = Views.getProviders({
            component: 'cockpit.processes.action'
          });
          $scope.hasActionPlugin = $scope.processesActions.length > 0;

          var processInstancePlugins = Views.getProviders({
            component: 'cockpit.processInstance.view'
          });
          $scope.hasHistoryPlugin =
            processInstancePlugins.filter(function(plugin) {
              return plugin.id === 'history';
            }).length > 0;
          $scope.hasReportPlugin =
            Views.getProviders({component: 'cockpit.report'}).length > 0;
          $scope.hasSearchPlugin =
            Views.getProviders({
              component: 'cockpit.processes.dashboard',
              id: 'search-process-instances'
            }).length > 0;

          // prettier-ignore
          $scope.headColumns = [
          { class: 'state',    request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_STATE')},
          { class: 'incidents',request: 'incidentCount', sortable: true,  content: $translate.instant('PLUGIN_PROCESS_DEF_INCIDENTS')},
          { class: 'instances',request: 'instances'    , sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_RUNNING_INSTANCES')},
          { class: 'name',     request: 'label' , sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_NAME')},
          { class: 'tenantID', request: 'tenantId'     , sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_TENANT_ID')},
          { class: 'history',  request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_HISTORY_VIEW'), condition: $scope.hasReportPlugin},
          { class: 'report',   request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_REPORT'), condition: $scope.hasReportPlugin},
          { class: 'action',   request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_ACTION'), condition: $scope.hasActionPlugin}
        ];

          // Default sorting
          var defaultValue = {
            sortBy: 'label',
            sortOrder: 'asc',
            sortReverse: false
          };

          $scope.sortObj = loadLocal(defaultValue);

          // Update Table
          $scope.onSortChange = function(sortObj) {
            sortObj = sortObj || $scope.sortObj;
            // transforms sortOrder to boolean required by anqular-sorting;
            sortObj.sortReverse = sortObj.sortOrder !== 'asc';
            saveLocal(sortObj);
            $scope.sortObj = sortObj;
          };

          var processData = $scope.processData.newChild($scope);

          var getPDIncidentsCount = function(incidents) {
            if (!incidents) {
              return 0;
            }
            return incidents.reduce(function(sum, incident) {
              return sum + incident.incidentCount;
            }, 0);
          };

          $scope.loadingState = 'LOADING';

          // only get count of process definitions
          var countProcessDefinitions = function() {
            processDefinitionService.count(
              {
                latest: true
              },
              function(err, count) {
                if (err) {
                  $scope.loadingState = 'ERROR';
                }
                $scope.processDefinitionsCount = count;
              }
            );
          };

          // get full list of process definitions and related resources
          var listProcessDefinitions = function() {
            $scope.loadingState = 'LOADING';

            processData.observe('processDefinitionStatistics', function(
              processDefinitionStatistics
            ) {
              $scope.processDefinitionData = processDefinitionStatistics.map(
                function(el) {
                  var definition = el.definition;
                  definition.label = definition.name || definition.key;
                  return definition;
                }
              );

              $scope.processDefinitionsCount =
                $scope.processDefinitionData.length;
              $scope.loadingState = 'LOADED';

              $scope.statistics = processDefinitionStatistics;

              $scope.statistics.forEach(function(statistic) {
                var processDefId = statistic.definition.id;
                var foundIds = $scope.processDefinitionData.filter(function(
                  pd
                ) {
                  return pd.id === processDefId;
                });

                var foundObject = foundIds[0];
                if (foundObject) {
                  foundObject.incidents = statistic.incidents;
                  foundObject.incidentCount = getPDIncidentsCount(
                    foundObject.incidents
                  );
                  foundObject.instances = statistic.instances;
                }
              });
            });
          };

          $scope.definitionVars = {read: ['pd']};

          var removeActionDeleteListener = $scope.$on(
            'processes.action.delete',
            function(event, definitionId) {
              var definitions = $scope.processDefinitionData;

              for (var i = 0; i < definitions.length; i++) {
                if (definitions[i].id === definitionId) {
                  definitions.splice(i, 1);
                  break;
                }
              }

              $scope.processDefinitionsCount = definitions.length;
            }
          );

          $scope.$on('$destroy', function() {
            removeActionDeleteListener();
          });

          $scope.incidentsLink =
            '#/processes?searchQuery=%5B%7B%22type%22:%22PIincidentStatus%22,%22operator%22:' +
            '%22eq%22,%22value%22:%22open%22,%22name%22:%22%22%7D,%7B%22type%22:%22PIprocessDefinitionKey%22,' +
            '%22operator%22:%22eq%22,%22value%22:%22PD_KEY%22,%22name%22:%22%22%7D%5D#search-section';

          $scope.activeTab = 'list';

          $scope.selectTab = function(tab) {
            $scope.activeTab = tab;
          };

          $scope.activeSection = localConf.get(
            'processesDashboardActive',
            true
          );
          // if tab is not active, it's enough to only get the count of process definitions
          $scope.activeSection
            ? listProcessDefinitions()
            : countProcessDefinitions();

          $scope.toggleSection = function toggleSection() {
            $scope.activeSection = !$scope.activeSection;

            // if tab is not active, it's enough to only get the count of process definitions
            $scope.activeSection
              ? listProcessDefinitions()
              : countProcessDefinitions();
            localConf.set('processesDashboardActive', $scope.activeSection);
          };

          function saveLocal(sortObj) {
            localConf.set('sortProcessDefTab', sortObj);
          }
          function loadLocal(defaultValue) {
            return localConf.get('sortProcessDefTab', defaultValue);
          }
        }
      ],

      priority: 0
    });
  }
];
