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

const angular = require('angular');
const template = require('./process-definitions.html?raw');
const searchConfig = require('./process-definition-search-config.json');

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
        '$location',
        'search',
        'PluginProcessDefinitionResource',
        function(
          $scope,
          Views,
          camAPI,
          localConf,
          $translate,
          $location,
          search,
          PluginProcessDefinitionResource
        ) {
          var processDefinitionService = camAPI.resource('process-definition');

          $scope.searchId = 'pdSearch';
          $scope.paginationId = 'pdPage';
          $scope.pages = null;
          $scope.query = null;
          $scope.sortBy = loadLocal({sortBy: 'name', sortOrder: 'asc'});
          $scope.config = searchConfig;
          $scope.onSearchChange = params =>
            updateView(params.query, params.pages);
          $scope.onSortChange = updateView;

          $scope.processesActions = Views.getProviders({
            component: 'cockpit.processes.action'
          });
          $scope.hasActionPlugin = $scope.processesActions.length > 0;

          const processInstancePlugins = Views.getProviders({
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
            { class: 'incidents',request: 'incidents', sortable: true,  content: $translate.instant('PLUGIN_PROCESS_DEF_INCIDENTS')},
            { class: 'instances',request: 'instances', sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_RUNNING_INSTANCES')},
            { class: 'key',      request: 'key', sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_KEY')},
            { class: 'name',     request: 'name', sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_NAME')},
            { class: 'tenantID', request: 'tenantId', sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_TENANT_ID')},
            { class: 'history',  request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_HISTORY_VIEW'), condition: $scope.hasReportPlugin},
            { class: 'report',   request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_REPORT'), condition: $scope.hasReportPlugin},
            { class: 'action',   request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_ACTION'), condition: $scope.hasActionPlugin}
          ];

          // only get count of process definitions
          const countProcessDefinitions = function() {
            $scope.mainLoadingState = 'LOADING';
            processDefinitionService.count({latest: true}, (err, count) => {
              if (err) $scope.mainLoadingState = 'ERROR';
              else $scope.mainLoadingState = count ? 'LOADED' : 'EMPTY';
              $scope.processDefinitionsCount = count;
            });
          };

          // get full list of process definitions and related resources
          function updateView(queryObj, pagesObj, sortObj) {
            $scope.loadingState = 'LOADING';
            $scope.query = queryObj || $scope.query;
            $scope.pages = pagesObj || $scope.pages;
            $scope.sortBy = sortObj || $scope.sortBy;

            const pagingParams = {
              firstResult: $scope.pages.size * ($scope.pages.current - 1),
              maxResults: $scope.pages.size
            };
            const queryParams = $scope.query;
            const sortParams = $scope.sortBy;
            const countParams = angular.extend({}, queryParams);
            const params = angular.extend(
              {},
              pagingParams,
              sortParams,
              queryParams
            );

            saveLocal(sortParams);

            return PluginProcessDefinitionResource.statisticsCount(countParams)
              .$promise.then(res => {
                $scope.total = res.count;

                PluginProcessDefinitionResource.queryStatistics(params)
                  .$promise.then(statistics => {
                    $scope.processDefinitionData = statistics;
                    $scope.loadingState = res.count ? 'LOADED' : 'EMPTY';
                    return statistics;
                  })
                  .catch(angular.noop);

                return $scope.total;
              })
              .catch(angular.noop);
          }

          $scope.definitionVars = {read: ['pd']};

          const removeActionDeleteListener = $scope.$on(
            'processes.action.delete',
            () => {
              countProcessDefinitions();
              updateView();
            }
          );

          $scope.$on('$destroy', function() {
            removeActionDeleteListener();
          });

          $scope.onIncidentsClick = pdKey => {
            const pdSearchQuery = $location.search().pdSearchQuery;
            const pdPage = $location.search().pdPage;
            const searchQuery = [
              {
                type: 'PIincidentStatus',
                operator: 'eq',
                value: 'open',
                name: ''
              },
              {
                type: 'PIprocessDefinitionKey',
                operator: 'eq',
                value: pdKey,
                name: ''
              }
            ];

            $location.search({
              searchQuery: JSON.stringify(searchQuery),
              pdSearchQuery,
              pdPage
            });
            $location.hash('search-section');
            $location.replace();
          };

          $scope.activeTab = 'list';

          $scope.selectTab = function(tab) {
            $scope.activeTab = tab;
          };

          $scope.activeSection = localConf.get(
            'processesDashboardActive',
            true
          );

          $scope.toggleSection = function toggleSection() {
            $scope.activeSection = !$scope.activeSection;
            localConf.set('processesDashboardActive', $scope.activeSection);

            if ($scope.activeSection) updateView();
          };

          function saveLocal(sortBy) {
            localConf.set('sortProcDefTab', sortBy);
          }
          function loadLocal(defaultValue) {
            return localConf.get('sortProcDefTab', defaultValue);
          }

          countProcessDefinitions();
        }
      ],

      priority: 0
    });
  }
];
