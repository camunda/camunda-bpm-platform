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

var template = require('./called-process-definition-table.html')();

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
            { class: 'called-process-state', request: 'state', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_STATE')},
            { class: 'activity', request: '[0].name', sortable: true, content: $translate.instant('PLUGIN_ACTIVITY')}
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
            [
              'calledProcessDefinitions',
              'staticCalledProcessDefinitions',
              'bpmnElements'
            ],
            function(
              calledProcessDefinitions,
              staticCalledProcessDefinitions,
              bpmnElements
            ) {
              const filteredStaticCalledDefs = applyFilterToStaticCalled(
                staticCalledProcessDefinitions
              );
              $scope.calledProcessDefinitions = createTableEntries(
                calledProcessDefinitions,
                filteredStaticCalledDefs,
                bpmnElements
              );

              $scope.loadingState = $scope.calledProcessDefinitions.length
                ? 'LOADED'
                : 'EMPTY';
            }
          );

          /**
           * Merges Dtos for currently running and statically linked called processes by their id.
           * @param runningProcessDefinitions
           * @param staticCalledProcesses
           * @returns {array}
           */
          function mergeInstanceAndDefinitionDtos(
            runningProcessDefinitions,
            staticCalledProcesses
          ) {
            const map = {};
            runningProcessDefinitions.forEach(dto => {
              const newDto = angular.copy(dto);
              newDto.state = 'PLUGIN_CALLED_PROCESS_DEFINITIONS_RUNNING_LABEL';
              map[newDto.id] = newDto;
            });

            staticCalledProcesses.forEach(dto => {
              const newDto = angular.copy(dto);
              if (map[dto.id]) {
                const merged = new Set([
                  ...map[newDto.id].calledFromActivityIds,
                  ...newDto.calledFromActivityIds
                ]);
                map[dto.id].calledFromActivityIds = Array.from(merged).sort();
                map[dto.id].state =
                  'PLUGIN_CALLED_PROCESS_DEFINITIONS_RUNNING_AND_REFERENCED_LABEL';
              } else {
                map[newDto.id] = newDto;
                newDto.state =
                  'PLUGIN_CALLED_PROCESS_DEFINITIONS_REFERENCED_LABEL';
                newDto.calledFromActivityIds.sort();
              }
            });
            return Object.values(map);
          }

          function applyFilterToStaticCalled(staticCalledDefinitions) {
            if (filter.activityIdIn && filter.activityIdIn.length) {
              const selectedIds = new Set(filter.activityIdIn);
              return staticCalledDefinitions
                .map(dto => {
                  const newDto = angular.copy(dto);
                  const intersection = dto.calledFromActivityIds.filter(e =>
                    selectedIds.has(e)
                  );
                  if (intersection.length) {
                    newDto.calledFromActivityIds = intersection;
                    return newDto;
                  }
                })
                .filter(dto => dto !== undefined);
            }
            return staticCalledDefinitions;
          }

          function createTableEntries(
            runningProcessDefinitions,
            staticCalledProcesses,
            bpmnElements
          ) {
            const mergedDefinitions = mergeInstanceAndDefinitionDtos(
              runningProcessDefinitions,
              staticCalledProcesses
            );

            const tableEntries = mergedDefinitions.map(dto => {
              const calledFromActivities = dto.calledFromActivityIds.map(id =>
                extractActivityFromDiagram(bpmnElements, id)
              );

              return angular.extend({}, dto, {
                calledFromActivities: calledFromActivities,
                label: dto.name || dto.key
              });
            });

            return tableEntries.map(dto => {
              if (
                tableEntries.find(
                  otherDto =>
                    dto.name === otherDto.name && otherDto.id !== dto.id
                )
              ) {
                dto.label = dto.label + ':' + dto.version;
              }
              return dto;
            });
          }

          /**
           * @param bpmnElements
           * @param activityId
           * @returns {{name: string, id: string}}
           */
          function extractActivityFromDiagram(bpmnElements, activityId) {
            const activityBpmnElement = bpmnElements[activityId];

            return {
              id: activityId,
              name:
                (activityBpmnElement && activityBpmnElement.name) || activityId
            };
          }
        }
      ],
      priority: 5
    });
  }
];
