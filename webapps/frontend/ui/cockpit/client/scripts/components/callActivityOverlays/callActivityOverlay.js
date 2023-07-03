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
const template = require('./template.html?raw');

module.exports = {
  getCallActivityFlowNodes: getCallActivityFlowNodes,
  addOverlayForSingleElement: addOverlayForSingleElement
};

/**
 *
 * @param {ElementRegistry} elementRegistry
 * @returns {Array} BPMN Elements that are flow nodes
 */
function getCallActivityFlowNodes(elementRegistry) {
  const nodes = [];

  elementRegistry.forEach(function(shape) {
    const bo = shape.businessObject;
    if (bo.$instanceOf('bpmn:CallActivity')) {
      nodes.push(bo.id);
    }
  });

  return nodes;
}
/**
 * @param {object} overlaysNodes
 * @param {string} activityId
 * @param {string|object} redirectionTarget
 * @param overlays
 * @param {function} clickListener
 * @param {string} tooltipTitle
 * @param $scope
 * @param $timeout
 */
function addOverlayForSingleElement({
  overlaysNodes,
  activityId,
  redirectionTarget,
  overlays,
  clickListener,
  tooltipTitle,
  $scope
}) {
  if (!overlaysNodes[activityId]) {
    const wrapper = angular.element(template);
    const button = wrapper.children().first();
    overlaysNodes[activityId] = wrapper;

    wrapper.tooltip({
      container: 'body',
      title: tooltipTitle,
      placement: 'top',
      animation: false
    });

    overlays.add(activityId, {
      position: {
        bottom: -7,
        right: -8
      },
      show: {
        minZoom: -Infinity,
        maxZoom: +Infinity
      },
      html: wrapper
    });

    addInteractions(
      wrapper,
      button,
      activityId,
      redirectionTarget,
      clickListener,
      $scope
    );
  }
}

/**
 * add hover and click interactions to buttonOverlay and diagramNode (BPMN diagram node that contains the buttonOverlay)
 * @param buttonOverlay anchor for the tooltip so the actual button can be disabled
 * @param button
 * @param id
 * @param redirectionTarget
 * @param clickListener
 * @param $scope
 * @param $timeout
 */
function addInteractions(
  buttonOverlay,
  button,
  id,
  redirectionTarget,
  clickListener,
  $scope
) {
  const diagramNode = angular.element('[data-element-id="' + id + '"]');

  if (redirectionTarget) {
    button.on('click', () => {
      clickListener(redirectionTarget);
    });
  } else {
    button.prop('disabled', true);
    button.css('pointer-events', 'none');
    buttonOverlay.css('cursor', 'not-allowed');
  }

  $scope.$on('$destroy', function() {
    // as these buttons happen outside of angular,
    // we need to clean up listeners and tooltip once we leave to avoid memory leaks
    button.off('click');
    buttonOverlay.tooltip('destroy');
    buttonOverlay.off('mouseenter mouseleave');
    diagramNode.off('mouseenter mouseleave');
  });
}
