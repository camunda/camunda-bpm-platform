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
    'camAPI',
    function(
      $scope,
      $timeout,
      $location,
      $translate,
      search,
      control,
      processData,
      camAPI,
    ) {
      /**
       * @returns {Array} BPMN Elements that are flow nodes
       */
      function getCallActivityFlowNodes() {
        var nodes = [];

        elementRegistry.forEach(function(shape) {
          var bo = shape.businessObject;
          if (bo.$instanceOf('bpmn:CallActivity')) {
            nodes.push(bo.id);
          }
        });

        return nodes;
      }

      /**
       * add hover and click interactions to buttonOverlay and diagramNode (BPMN diagram node that contains the buttonOverlay)
       * @param buttonOverlay
       * @param id
       * @param calledProcessDefinitionId
       */
      function addInteractions(buttonOverlay, id, calledProcessDefinitionId) {
        var diagramNode = angular.element('[data-element-id="' + id + '"]');
        var hideTimeout = null;

        /**
         * calls function dynamically and make sure to call $scope.apply
         */
        var applyFunction = function() {
          arguments[0].apply(this, Array.prototype.slice.call(arguments, 1));
          var phase = $scope.$root.$$phase;
          if (phase !== '$apply' && phase !== '$digest') {
            $scope.$apply();
          }
        };

        /**
         * hide buttonOverlay after delay time
         * @param delay
         */
        var delayHide = function(delay) {
          hideTimeout = $timeout(function() {
            buttonOverlay.hide();
          }, delay);
        };

        /**
         * cancels timeout object
         */
        var cancelHide = function() {
          return hideTimeout && $timeout.cancel(hideTimeout);
        };

        var mouseoverListener = function() {
          buttonOverlay.show();
          applyFunction(cancelHide);
        };

        var redirectToCalledPInstance = function(id) {
          const url =
            '/process-definition/' +
            id +
            '/' +
            viewContext;
          $location.url(url);
        };

        var clickListener = function() {
          buttonOverlay.tooltip('hide');
          return applyFunction(redirectToCalledPInstance, calledProcessDefinitionId);
        };

        // attach diagramNode listeners
        diagramNode.on('mouseover', mouseoverListener);
        diagramNode.on('mouseout', function() {
          delayHide(50);
        });

        // attach buttonOverlay listeners
        buttonOverlay.on('mouseover', mouseoverListener);
        buttonOverlay.on('mouseout', function() {
          delayHide(100);
        });

        if (calledProcessDefinitionId) {
          buttonOverlay.on('click', clickListener);
        } else {
          buttonOverlay.css('opacity', '0.6');
          //buttonOverlay.prop('disabled', true);
        }

        // clear listeners
        $scope.$on('$destroy', function() {
          buttonOverlay.off('mouseover mouseout click');
          diagramNode.off('mouseover mouseout');
        });
      }

      /**
       *
       * @param callActivityId (BPMN element id)
       * @param calledProcessDefinitionId (activity associated with that id)
       */
      function addOverlayForSingleElement(callActivityId, calledProcessDefinitionId) {
        if (!overlaysNodes[callActivityId]) {
          overlaysNodes[callActivityId] = angular.element(template).hide();
          const isStatic = calledProcessDefinitionId;
          // Todo add localizable text constant
          const text = calledProcessDefinitionId ? 'Show statically linked process definition': 'Linked process definition is resolved at runtime'
          overlaysNodes[callActivityId].tooltip({
            container: 'body',
            title: $translate.instant(
              text
            ),
            placement: 'top',
            animation: false
          });

          overlays.add(callActivityId, {
            position: {
              top: 0,
              right: 0
            },
            show: {
              minZoom: -Infinity,
              maxZoom: +Infinity
            },
            html: overlaysNodes[callActivityId]
          });
          addInteractions(overlaysNodes[callActivityId], callActivityId, calledProcessDefinitionId);
        }
      }

      const overlaysNodes = {};
      const overlays = control.getViewer().get('overlays');
      const elementRegistry = control.getViewer().get('elementRegistry');
      const callActivityFlowNodes = getCallActivityFlowNodes();


      if(callActivityFlowNodes.length) {
        processData.provide(
          'staticCalledProcessDefinitions',
          ['processDefinition',
          function (processDefinition) {
            const ProcessDefinition = camAPI.resource('process-definition');
            return ProcessDefinition.linkedCallableElements(processDefinition.id);
          }]
        )

        processData.observe(
          ['processDefinition', 'staticCalledProcessDefinitions'],
          function (processDefinition, staticCalledProcessDefinitions) {
            const ProcessDefinition = camAPI.resource('process-definition');
            const callActivityToProcessMap = {};
            const drawStaticLinks = function (staticProcDefs) {
              for (const dto of staticProcDefs) {
                dto.callActivityIds.forEach(callerId => callActivityToProcessMap[callerId] = dto)
              }
              for (const activity of callActivityFlowNodes) {
                if(callActivityToProcessMap[activity]){
                  addOverlayForSingleElement(activity, callActivityToProcessMap[activity].id);
                } else {
                  addOverlayForSingleElement(activity, undefined);
                }
              }
            }
            drawStaticLinks(staticCalledProcessDefinitions);
          }
        );
      }

    }
  ];
};
