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
const util = require('../callActivityOverlays/callActivityOverlay')

var template = fs.readFileSync(__dirname + '/template.html', 'utf8');

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
      processData,
    ) {

      const redirectToCalledPInstance = function (calledProcessId) {
        const url =
          '/process-definition/' +
          calledProcessId +
          '/runtime?parentProcessDefinitionId=' +
          $scope.key;
        $location.url(url);
      };

      const clickListener = function (buttonOverlay, applyFunction, calledProcessDefinitionId) {
        buttonOverlay.tooltip('hide');
        return applyFunction(redirectToCalledPInstance, calledProcessDefinitionId);
      };


      const overlaysNodes = {};
      const overlays = control.getViewer().get('overlays');
      const elementRegistry = control.getViewer().get('elementRegistry');
      const callActivityFlowNodes = util.getCallActivityFlowNodes(elementRegistry);


      if(callActivityFlowNodes.length) {
        processData.observe(
          ['processDefinition', 'staticCalledProcessDefinitions'],
          function (processDefinition, staticCalledProcessDefinitions) {
            const callActivityToProcessMap = {};
            const drawStaticLinks = function (staticProcDefs) {
              for (const dto of staticProcDefs) {
                dto.calledFromActivityIds.forEach(callerId => callActivityToProcessMap[callerId] = dto);
              }
              for (const activity of callActivityFlowNodes) {
                if(callActivityToProcessMap[activity]){
                  util.addOverlayForSingleElement(overlaysNodes, activity, callActivityToProcessMap[activity].id, control, clickListener, $translate,  $scope, $timeout);
                } else {
                  util.addOverlayForSingleElement(overlaysNodes, activity, undefined, control, clickListener, $translate, $scope, $timeout);
                }
              }
            };
            drawStaticLinks(staticCalledProcessDefinitions);
          }
        );
      }

    }
  ];
};
