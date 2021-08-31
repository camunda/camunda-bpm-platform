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
const fs = require('fs');
const angular = require('angular');
const template = fs.readFileSync(__dirname + '/template.html', 'utf8');

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
function addOverlayForSingleElement(
  overlaysNodes,
  activityId,
  redirectionTarget,
  overlays,
  clickListener,
  tooltipTitle,
  $scope,
  $timeout
) {
  if (!overlaysNodes[activityId]) {
    const wrapper = angular.element(template).hide();
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
        top: 0,
        right: 0
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
      $scope,
      $timeout
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
  $scope,
  $timeout
) {
  const diagramNode = angular.element('[data-element-id="' + id + '"]');
  let timeoutPromise = null;

  /**
   * hide buttonOverlay after delay time
   * @param delay
   */
  const hideWithDelay = function(delay) {
    timeoutPromise = $timeout(function() {
      buttonOverlay.hide();
    }, delay);
  };

  /**
   * cancels timeout object
   */
  const cancelHide = function() {
    return timeoutPromise && $timeout.cancel(timeoutPromise);
  };

  const mouseoverListener = function() {
    cancelHide();
    buttonOverlay.show();
  };

  // attach diagramNode listeners
  diagramNode.on('mouseenter', mouseoverListener);
  diagramNode.on('mouseleave', function() {
    hideWithDelay(50);
  });

  // attach buttonOverlay listeners
  buttonOverlay.on('mouseenter', mouseoverListener);
  buttonOverlay.on('mouseleave', function() {
    hideWithDelay(100);
  });

  if (redirectionTarget) {
    button.on('click', () => {
      buttonOverlay.tooltip('hide');
      clickListener(redirectionTarget);
    });
  } else {
    button.prop('disabled', true);
    button.css('pointer-events', 'none');
    buttonOverlay.css('cursor', 'not-allowed');
  }

  // clear listeners
  $scope.$on('$destroy', function() {
    button.off('click');
    buttonOverlay.off('mouseenter mouseleave');
    diagramNode.off('mouseenter mouseleave');
  });
}
