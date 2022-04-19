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
var angular = require('angular');

var template = require('./decision-instance-table.html')();
var decisionSearchConfig = require('./decision-instance-search-config.json');

var debouncePromiseFactory = require('camunda-bpm-sdk-js').utils
  .debouncePromiseFactory;
var debounceQuery = debouncePromiseFactory();

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.decisionDefinition.tab', {
      id: 'decision-instances-table',
      label: 'DECISION_DEFINITION_LABEL',
      template: template,
      controller: [
        '$scope',
        '$location',
        'search',
        'routeUtil',
        'camAPI',
        'Views',
        '$translate',
        'localConf',
        function(
          $scope,
          $location,
          search,
          routeUtil,
          camAPI,
          Views,
          $translate,
          localConf
        ) {
          // prettier-ignore
          $scope.headColumns = [
          { class: 'instance-id',    request: ''          , sortable: false, content: $translate.instant('PLUGIN_DECISION_ID')},
          { class: 'start-time',     request: 'evaluationTime'     , sortable: true, content: $translate.instant('PLUGIN_DECISION_EVALUATION_TIME')},
          { class: 'definition-key', request: '', sortable: false, content: $translate.instant('PLUGIN_DECISION_CALLING_PROCESS_CASE')},
          { class: 'instance-id',    request: '', sortable: false, content: $translate.instant('PLUGIN_DECISION_CALLING_INSTANCE_ID')},
          { class: 'activity-id',    request: '', sortable: false, content: $translate.instant('PLUGIN_DECISION_ACTIVITY_ID')}
        ];

          // Default sorting
          var defaultValue = {sortBy: 'evaluationTime', sortOrder: 'desc'};
          $scope.sortObj = loadLocal(defaultValue);

          var processInstancePlugins = Views.getProviders({
            component: 'cockpit.processInstance.view'
          });
          var hasHistoryPlugin =
            processInstancePlugins.filter(function(plugin) {
              return plugin.id === 'history';
            }).length > 0;

          $scope.hasCasePlugin = false;
          try {
            $scope.hasCasePlugin = !!angular.module('cockpit.plugin.case');
          } catch (e) {
            // do nothing
          }

          $scope.getProcessDefinitionLink = function(decisionInstance) {
            if (hasHistoryPlugin) {
              return (
                '#/process-definition/' +
                decisionInstance.processDefinitionId +
                '/history'
              );
            } else {
              return (
                '#/process-definition/' + decisionInstance.processDefinitionId
              );
            }
          };

          $scope.getProcessInstanceLink = function(decisionInstance) {
            if (hasHistoryPlugin) {
              return (
                '#/process-instance/' +
                decisionInstance.processInstanceId +
                '/history' +
                '?activityInstanceIds=' +
                decisionInstance.activityInstanceId +
                '&activityIds=' +
                decisionInstance.activityId
              );
            } else {
              return '#/process-instance/' + decisionInstance.processInstanceId;
            }
          };

          $scope.getActivitySearch = function(decisionInstance) {
            return JSON.stringify([
              {
                type: 'caseActivityIdIn',
                operator: 'eq',
                value: decisionInstance.activityId
              }
            ]);
          };

          $scope.searchConfig = angular.copy(decisionSearchConfig);

          var historyService = camAPI.resource('history');

          $scope.onSearchChange = updateView;
          $scope.onSortChange = updateView;

          function updateView(searchQuery, pages, sortObj) {
            $scope.pagesObj = pages || $scope.pagesObj;
            $scope.sortObj = sortObj || $scope.sortObj;

            // Add default sorting param
            if (sortObj) {
              saveLocal(sortObj);
            }

            var page = $scope.pagesObj.current,
              count = $scope.pagesObj.size,
              firstResult = (page - 1) * count;

            $scope.decisionInstances = null;
            $scope.loadingState = 'LOADING';

            var decisionInstanceQuery = angular.extend(
              {
                decisionDefinitionId: $scope.decisionDefinition.id,
                firstResult: firstResult,
                maxResults: count,
                sortBy: $scope.sortObj.sortBy,
                sortOrder: $scope.sortObj.sortOrder
              },
              searchQuery
            );

            var countQuery = angular.extend(
              {
                decisionDefinitionId: $scope.decisionDefinition.id
              },
              searchQuery
            );

            return debounceQuery(
              historyService
                .decisionInstanceCount(countQuery)
                .then(function(count) {
                  var total = count.count;

                  return historyService
                    .decisionInstance(decisionInstanceQuery)

                    .then(function(data) {
                      return {total, data};
                    });
                })
            )
              .then(({total, data}) => {
                $scope.decisionInstances = data;
                $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

                var phase = $scope.$root.$$phase;
                if (phase !== '$apply' && phase !== '$digest') {
                  $scope.$apply();
                }

                return total;
              })
              .catch(angular.noop);
          }

          function saveLocal(sortObj) {
            localConf.set('sortDecInstTab', sortObj);
          }
          function loadLocal(defaultValue) {
            return localConf.get('sortDecInstTab', defaultValue);
          }
        }
      ],
      priority: 10
    });
  }
];
