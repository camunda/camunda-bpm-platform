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

module.exports = function(viewContext) {
  return [
    '$scope',
    '$timeout',
    '$location',
    '$translate',
    'search',
    'control',
    'processData',
    function(
      $scope,
      $timeout,
      $location,
      $translate,
      search,
      control,
      processData
    ) {
      const redirectToCalledDefinition = function(calledProcessId) {
        const url =
          '/process-definition/' +
          calledProcessId +
          '/' +
          viewContext +
          '?parentProcessDefinitionId=' +
          $scope.key;
        $location.url(url);
      };

      const clickListener = function(buttonOverlay, calledProcessDefinitionId) {
        buttonOverlay.tooltip('hide');
        return $scope.$apply(() =>
          redirectToCalledDefinition(calledProcessDefinitionId)
        );
      };

      const overlaysNodes = {};
      const overlays = control.getViewer().get('overlays');
      const elementRegistry = control.getViewer().get('elementRegistry');
      const callActivityFlowNodes = getCallActivityFlowNodes(elementRegistry);

      if (callActivityFlowNodes.length) {
        processData.observe(
          ['processDefinition', 'staticCalledProcessDefinitions'],
          function(processDefinition, staticCalledProcessDefinitions) {
            const callActivityToProcessMap = {};
            const drawStaticLinks = function(staticProcDefs) {
              for (const dto of staticProcDefs) {
                dto.calledFromActivityIds.forEach(
                  callerId => (callActivityToProcessMap[callerId] = dto)
                );
              }
              const resolvable = $translate.instant(
                'PLUGIN_ACTIVITY_DEFINITION_SHOW_CALLED_PROCESS_DEFINITION'
              );
              const notResolvable = $translate.instant(
                'PLUGIN_ACTIVITY_DEFINITION_CALLED_NOT_RESOLVABLE'
              );
              for (const activity of callActivityFlowNodes) {
                const calledProcess = callActivityToProcessMap[activity]
                  ? callActivityToProcessMap[activity].id
                  : undefined;

                const toolTipTitle = calledProcess ? resolvable : notResolvable;

                addOverlayForSingleElement(
                  overlaysNodes,
                  activity,
                  calledProcess,
                  overlays,
                  clickListener,
                  toolTipTitle,
                  $scope,
                  $timeout
                );
              }
            };

            drawStaticLinks(staticCalledProcessDefinitions);
          }
        );
      }
    }
  ];
};
