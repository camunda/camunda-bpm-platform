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

var angular = require('angular');

var template = require('./process-instance-table.html?raw');
var searchConfig = require('./process-instance-search-config.json');

var debouncePromiseFactory = require('camunda-bpm-sdk-js').utils
  .debouncePromiseFactory;
var debouncePromise = debouncePromiseFactory();

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
      id: 'process-instances-table',
      label: 'PLUGIN_PROCESS_INSTANCES_LABEL',
      template: template,
      controller: [
        '$scope',
        '$location',
        'search',
        'routeUtil',
        'PluginProcessInstanceResource',
        '$translate',
        'localConf',
        function(
          $scope,
          $location,
          search,
          routeUtil,
          PluginProcessInstanceResource,
          $translate,
          localConf
        ) {
          var processDefinition = $scope.processDefinition;
          $scope.onSearchChange = updateView;
          $scope.onSortChange = updateView;

          // prettier-ignore
          $scope.headColumns = [
          { class: 'state',        request: 'state',       sortable: false, content: $translate.instant('PLUGIN_PROCESS_INSTANCE_STATE')},
          { class: 'instance-id',  request: 'instanceId',  sortable: false, content: $translate.instant('PLUGIN_PROCESS_INSTANCE_ID')},
          { class: 'start-time',   request: 'startTime',   sortable: true,  content: $translate.instant('PLUGIN_PROCESS_INSTANCE_START_TIME')},
          { class: 'business-key', request: 'businessKey', sortable: false, content: $translate.instant('PLUGIN_PROCESS_INSTANCE_BUSINESS_KEY')}
        ];

          // Default sorting
          var defaultValue = {sortBy: 'startTime', sortOrder: 'desc'};
          $scope.sortObj = loadLocal(defaultValue);

          $scope.searchConfig = angular.copy(searchConfig);

          function updateView(query, pages, sortObj) {
            $scope.pagesObj = pages || $scope.pagesObj;
            $scope.queryObj = query || $scope.queryObj;
            sortObj = sortObj || $scope.sortObj;

            saveLocal(sortObj);

            var page = $scope.pagesObj.current,
              queryParams = $scope.queryObj,
              count = $scope.pagesObj.size,
              firstResult = (page - 1) * count;

            var defaultParams = {
              processDefinitionId: processDefinition.id
            };

            var pagingParams = {
              firstResult: firstResult,
              maxResults: count,
              sortBy: sortObj.sortBy,
              sortOrder: sortObj.sortOrder
            };

            var countParams = angular.extend({}, queryParams, defaultParams);
            var params = angular.extend(
              {},
              queryParams,
              pagingParams,
              defaultParams
            );

            $scope.processInstances = null;
            $scope.loadingState = 'LOADING';

            return PluginProcessInstanceResource.count(countParams)
              .$promise.then(function(data) {
                var total = data.count;

                return debouncePromise(
                  PluginProcessInstanceResource.query(pagingParams, params)
                    .$promise
                )
                  .then(function(data) {
                    $scope.processInstances = data;
                    $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

                    var phase = $scope.$root.$$phase;
                    if (phase !== '$apply' && phase !== '$digest') {
                      $scope.$apply();
                    }

                    return total;
                  })
                  .catch(angular.noop);
              })
              .catch(angular.noop);
          }

          function saveLocal(sortObj) {
            localConf.set('sortProcInst', sortObj);
          }

          function loadLocal(defaultValue) {
            return localConf.get('sortProcInst', defaultValue);
          }

          $scope.getProcessInstanceUrl = function(processInstance, params) {
            var path = '#/process-instance/' + processInstance.id;
            var searches = angular.extend(
              {},
              $location.search() || {},
              params || {}
            );

            var keepSearchParams = ['viewbox'];
            for (var i in params) {
              keepSearchParams.push(i);
            }

            return routeUtil.redirectTo(path, searches, keepSearchParams);
          };
        }
      ],
      priority: 10
    });
  }
];
