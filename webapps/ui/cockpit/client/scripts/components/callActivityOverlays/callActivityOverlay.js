'use strict';
const fs = require('fs');
const angular = require('angular');
const template = fs.readFileSync(__dirname + '/template.html', 'utf8');


module.exports = {
  getCallActivityFlowNodes: getCallActivityFlowNodes,
  addOverlayForSingleElement: addOverlayForSingleElement
}

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
 * add hover and click interactions to buttonOverlay and diagramNode (BPMN diagram node that contains the buttonOverlay)
 * @param buttonOverlay
 * @param id
 * @param calledProcesses
 * @param clickListener
 * @param $scope
 * @param $timeout
 */
function addInteractions(buttonOverlay, id, calledProcesses, clickListener, $scope, $timeout) {
  const diagramNode = angular.element('[data-element-id="' + id + '"]');
  let hideTimeout = null;

  /**
   * calls function dynamically and make sure to call $scope.apply
   */
  const applyFunction = function() {
    arguments[0].apply(this, Array.prototype.slice.call(arguments, 1));
    const phase = $scope.$root.$$phase;
    if (phase !== '$apply' && phase !== '$digest') {
      $scope.$apply();
    }
  };

  /**
   * hide buttonOverlay after delay time
   * @param delay
   */
  const delayHide = function(delay) {
    hideTimeout = $timeout(function() {
      buttonOverlay.hide();
    }, delay);
  };

  /**
   * cancels timeout object
   */
  const cancelHide = function() {
    return hideTimeout && $timeout.cancel(hideTimeout);
  };

  const mouseoverListener = function() {
    buttonOverlay.show();
    applyFunction(cancelHide);
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

  if (calledProcesses) {
    buttonOverlay.on('click', () => clickListener(buttonOverlay, applyFunction, calledProcesses));
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
//check outside that overlays is not set yet????
/**
 *
 * @param {object} overlaysNodes
 * @param {string} activityId
 * @param calledProcesses
 * @param control
 * @param {function} clickListener
 * @param $translate
 */
function addOverlayForSingleElement(overlaysNodes, activityId, calledProcesses, control, clickListener, $translate,  $scope, $timeout) {
  const overlays = control.getViewer().get('overlays');

  if (!overlaysNodes[activityId]) {
    overlaysNodes[activityId] = angular.element(template).hide();

    overlaysNodes[activityId].tooltip({
      container: 'body',
      title: $translate.instant(
        'PLUGIN_ACTIVITY_INSTANCE_SHOW_CALLED_PROCESS_INSTANCES'
      ),
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
      html: overlaysNodes[activityId]
    });

    addInteractions(overlaysNodes[activityId], activityId, calledProcesses, clickListener, $scope, $timeout);

    //maybe just return overlay instead of passing all functions?

  }
}
