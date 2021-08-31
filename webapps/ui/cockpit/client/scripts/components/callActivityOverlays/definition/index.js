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

const {
  getCallActivityFlowNodes,
  addOverlayForSingleElement
} = require('../callActivityOverlay');
const angular = require('angular');

module.exports = function(viewContext) {
  return [
    '$scope',
    '$timeout',
    '$location',
    '$translate',
    'search',
    'control',
    'processData',
    'PluginProcessDefinitionResource',
    function(
      $scope,
      $timeout,
      $location,
      $translate,
      search,
      control,
      processData,
      PluginProcessDefinitionResource
    ) {
      const redirectToCalledDefinition = function(calledProcessId) {
        return $scope.$apply(() => {
          const url = `/process-definition/${calledProcessId}/${viewContext}?parentProcessDefinitionId=${$scope.key}`;
          $location.url(url);
        });
      };

      /**
       * shows calledProcessDefinitions tab filtered by activityId. This is used in case a currently calling activity
       * calls different process definitions.
       * @param activityId
       */
      const showCalledDefinitionsInTable = function(activityId) {
        return $scope.$apply(() => {
          const params = angular.copy(search());
          params.detailsTab = 'call-process-definitions-table';
          search.updateSilently(params);
          $scope.processData.set('filter', {
            activityIds: [activityId]
          });
        });
      };

      const overlaysNodes = {};
      const overlays = control.getViewer().get('overlays');
      const elementRegistry = control.getViewer().get('elementRegistry');
      const callActivityFlowNodes = getCallActivityFlowNodes(elementRegistry);
      const resolvable = $translate.instant(
        'PLUGIN_ACTIVITY_DEFINITION_SHOW_CALLED_PROCESS_DEFINITION'
      );
      const notResolvable = $translate.instant(
        'PLUGIN_ACTIVITY_DEFINITION_CALLED_NOT_RESOLVABLE'
      );
      const dynamicResolve = $translate.instant(
        'PLUGIN_ACTIVITY_DEFINITION_CALLED_DYNAMIC_RESOLVABLE'
      );
      const dynamicMultipleResolve = $translate.instant(
        'PLUGIN_ACTIVITY_DEFINITION_CALLED_DYNAMIC_MULTI_RESOLVABLE'
      );

      if (callActivityFlowNodes.length) {
        processData.observe(
          ['processDefinition', 'staticCalledProcessDefinitions'],
          function(processDefinition, staticCalledProcessDefinitions) {
            PluginProcessDefinitionResource.getCalledProcessDefinitions({
              id: processDefinition.id
            })
              .$promise.then(dynamicCalledProcessDefinitions =>
                drawLinks(
                  staticCalledProcessDefinitions,
                  dynamicCalledProcessDefinitions
                )
              )
              .catch(angular.noop);
          }
        );
      }
      const drawLinks = function(staticProcDefs, dynamicProcDefs) {
        const callActivityToProcessMap = {};
        for (const dto of staticProcDefs) {
          dto.calledFromActivityIds.forEach(
            callerId => (callActivityToProcessMap[callerId] = dto)
          );
        }
        const dynamicCallActivityToProcessesMap = {};
        for (const dto of dynamicProcDefs) {
          dto.calledFromActivityIds.forEach(callerId => {
            if (dynamicCallActivityToProcessesMap[callerId]) {
              dynamicCallActivityToProcessesMap[callerId].push(dto);
            } else {
              dynamicCallActivityToProcessesMap[callerId] = [dto];
            }
          });
        }

        for (const activity of callActivityFlowNodes) {
          let redirectToId = callActivityToProcessMap[activity]
            ? callActivityToProcessMap[activity].id
            : undefined;
          let toolTipTitle = redirectToId ? resolvable : notResolvable;
          let clickListener = redirectToCalledDefinition;
          if (!redirectToId && viewContext === 'runtime') {
            /* We only link currently running dynamic call activities in the runtime view,
            because the history view has no calledProcessDefinitionTable we can redirect to if there are different
            process definitions called by one dynamic call activity.
             */
            if (dynamicCallActivityToProcessesMap[activity]) {
              if (dynamicCallActivityToProcessesMap[activity].length > 1) {
                redirectToId = activity;
                clickListener = showCalledDefinitionsInTable;
                toolTipTitle = dynamicMultipleResolve;
              } else {
                redirectToId =
                  dynamicCallActivityToProcessesMap[activity][0].id;
                toolTipTitle = dynamicResolve;
              }
            }
          }

          addOverlayForSingleElement(
            overlaysNodes,
            activity,
            redirectToId,
            overlays,
            clickListener,
            toolTipTitle,
            $scope,
            $timeout
          );
        }
      };
    }
  ];
};
