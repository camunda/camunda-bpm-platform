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
            { class: 'process-definition-running', request: 'running', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_RUNNING')},
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
            ['calledProcessDefinitions', 'staticCalledProcessDefinitions', 'bpmnElements'],
            function(calledProcessDefinitions, staticCalledProcessDefinitions, bpmnElements) {
              console.log(staticCalledProcessDefinitions);
              let staticCalled = [...staticCalledProcessDefinitions];
              if(filter.activityIdIn && filter.activityIdIn.length) {
                const filteredIds = new Set(filter.activityIdIn)
                staticCalled = staticCalled.map(dto => {
                    const newDto = angular.copy(dto)
                    const intersection = dto.calledFromActivityIds.filter(e => filteredIds.has(e))
                    if (intersection.length) {
                      newDto.calledFromActivityIds = intersection;
                      return newDto
                    }
                  }
                ).filter(e => e !== undefined);
              }
              let mergedDefinitions =  attachCalledFromActivities(
                calledProcessDefinitions,
                staticCalled,
                bpmnElements
              ).map(function(calledProcessDefinition) {
                return angular.extend({}, calledProcessDefinition, {
                  label:
                    calledProcessDefinition.name || calledProcessDefinition.key
                });
              } );
              mergedDefinitions = mergedDefinitions.map(dto => {
                if(mergedDefinitions.find(e => e.id !== dto.id && dto.name === e.name)){
                  dto.label = dto.label + ":"  + dto.version;
                }
                return dto;
              })
              $scope.calledProcessDefinitions = mergedDefinitions;
              $scope.loadingState = $scope.calledProcessDefinitions.length
                ? 'LOADED'
                : 'EMPTY';
            }
          );

          function attachCalledFromActivities(
            runningProcessDefinitions,
            staticCalledProcesses,
            bpmnElements
          ) {
            const result = [];
            /// merge todo factor out
            const map = {}
            runningProcessDefinitions.forEach(
              dto => {
                const newDto = angular.copy(dto);
                newDto.running = "Running";
                map[newDto.id] = newDto;
              }
            )

            staticCalledProcesses.forEach(
              dto => {
                const newDto = angular.copy(dto);
                if(map[dto.id]){
                  const merged = new Set([...map[newDto.id].calledFromActivityIds, ...newDto.calledFromActivityIds])
                  map[dto.id].calledFromActivityIds = Array.from(merged).sort();
                  map[dto.id].running = "Running and Referenced";
                  // add static flag?
                } else {
                  map[newDto.id] = newDto;
                  newDto.running = "Referenced";
                  newDto.calledFromActivityIds.sort();
                }
              }
            )

            const definitions = Object.values(map);

            definitions.forEach(function(d) {
              var calledFromActivityIds = d.calledFromActivityIds;
              const calledFromActivities = [];

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
