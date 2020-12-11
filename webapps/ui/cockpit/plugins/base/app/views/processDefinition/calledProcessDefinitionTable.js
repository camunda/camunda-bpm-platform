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
var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');

var template = fs.readFileSync(
  __dirname + '/called-process-definition-table.html',
  'utf8'
);

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
      id: 'call-process-definitions-table',
      label: 'PLUGIN_CALLED_PROCESS_DEFINITIONS_LABEL',
      template: template,
      controller: [
        '$scope',
        '$location',
        '$q',
        'PluginProcessDefinitionResource',
        '$translate',
        'localConf',
        function(
          $scope,
          $location,
          $q,
          PluginProcessDefinitionResource,
          $translate,
          localConf
        ) {
          var filter;
          var processData = $scope.processData.newChild($scope);

          // prettier-ignore
          $scope.headColumns = [
          { class: 'process-definition', request: 'label', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS')},
          { class: 'activity', request: 'name', sortable: true, content: $translate.instant('PLUGIN_ACTIVITY')}
        ];

          // Default sorting
          $scope.sortObj = loadLocal({
            sortBy: 'key',
            sortOrder: 'asc',
            sortReverse: false
          });

          $scope.onSortChange = function(sortObj) {
            sortObj = sortObj || $scope.sortObj;
            // sortReverse required by anqular-sorting;
            sortObj.sortReverse = sortObj.sortOrder !== 'asc';
            saveLocal(sortObj);
            $scope.sortObj = sortObj;
          };

          function saveLocal(sortObj) {
            localConf.set('sortCalledProcessDefTab', sortObj);
          }
          function loadLocal(defaultValue) {
            return localConf.get('sortCalledProcessDefTab', defaultValue);
          }

          $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(
            null,
            'activityIdIn'
          );

          processData.provide('calledProcessDefinitions', [
            'processDefinition',
            'filter',
            function(processDefinition, newFilter) {
              filter = angular.copy(newFilter);

              delete filter.page;
              delete filter.scrollToBpmnElement;

              // the parent process definition id is the super process definition id...
              filter.superProcessDefinitionId =
                filter.parentProcessDefinitionId;
              // ...and the process definition id of the current view is the
              // parent process definition id of query.
              filter.parentProcessDefinitionId = $scope.processDefinition.id;

              filter.activityIdIn = filter.activityIds;
              delete filter.activityIds;

              $scope.loadingState = 'LOADING';
              return PluginProcessDefinitionResource.getCalledProcessDefinitions(
                {id: processDefinition.id},
                filter
              ).$promise;
            }
          ]);

          processData.observe(
            ['calledProcessDefinitions', 'bpmnElements'],
            function(calledProcessDefinitions, bpmnElements) {
              $scope.calledProcessDefinitions = attachCalledFromActivities(
                calledProcessDefinitions,
                bpmnElements
              ).map(function(calledProcessDefinition) {
                return angular.extend({}, calledProcessDefinition, {
                  label:
                    calledProcessDefinition.name || calledProcessDefinition.key
                });
              });
              $scope.loadingState = $scope.calledProcessDefinitions.length
                ? 'LOADED'
                : 'EMPTY';
            }
          );

          function attachCalledFromActivities(
            processDefinitions,
            bpmnElements
          ) {
            var result = [];

            angular.forEach(processDefinitions, function(d) {
              var calledFromActivityIds = d.calledFromActivityIds,
                calledFromActivities = [];

              angular.forEach(calledFromActivityIds, function(activityId) {
                var bpmnElement = bpmnElements[activityId];

                var activity = {
                  id: activityId,
                  name: (bpmnElement && bpmnElement.name) || activityId
                };

                calledFromActivities.push(activity);
              });

              result.push(
                angular.extend({}, d, {
                  calledFromActivities: calledFromActivities
                })
              );
            });

            return result;
          }
        }
      ],
      priority: 5
    });
  }
];
