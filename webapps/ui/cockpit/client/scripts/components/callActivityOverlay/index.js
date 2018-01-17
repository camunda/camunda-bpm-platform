var fs = require('fs');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/template.html', 'utf8');

module.exports = [
  '$scope', '$timeout', '$location', 'search', 'control', 'processData', 'processDiagram',
  function($scope, $timeout, $location, search, control, processData, processDiagram) {
    /**
     * @returns {Array} BPMN Elements that are flow nodes
     */
    function getFlowNodes() {
      var bpmnElements = processDiagram.bpmnElements;
      return Object.keys(processDiagram.bpmnElements)
        .filter(function(key) {
          return bpmnElements[key].$instanceOf('bpmn:CallActivity');
        });
    }

    /**
     * shows calledProcessInstances tab filtered by activityId
     * @param activityId
     */
    function showCalledPInstances(activities) {
      var params = angular.copy(search());
      params.detailsTab = TAB_NAME;
      search.updateSilently(params);

      $scope.processData.set('filter', {
        activityIds: [ activities[0].activityId ],
        activityInstanceIds: activities.map(function(activity) {
          return activity.id;
        })
      });
    }

    /**
     * add hover and click interactions to buttonOverlay and diagramNode (BPMN diagram node that contains the buttonOverlay)
     * @param buttonOverlay
     * @param id
     * @param activityInstances (callActivity instances)
     */
    function addInteractions(buttonOverlay, id, activityInstances) {
      var diagramNode = angular.element('[data-element-id="' + id + '"]');
      var hideTimeout = null;

      /**
       * calls function dynamically and make sure to call $scope.apply
       */
      var applyFunction = function() {
        arguments[0].apply(this, Array.prototype.slice.call(arguments, 1));
        var phase = $scope.$root.$$phase;
        if(phase !== '$apply' && phase !== '$digest') {
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

      var redirectToCalledPInstance = function(activityInstance) {
        var view = activityInstance.endTime ?
          '/history' :
          '/runtime';
        $location.url('/process-instance/' + activityInstance.calledProcessInstanceId + view);
      };

      var clickListener = function() {
        return activityInstances.length > 1 ?
          applyFunction(showCalledPInstances, activityInstances):
          applyFunction(redirectToCalledPInstance, activityInstances[0]);
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
      buttonOverlay.on('click', clickListener);

      // clear listeners
      $scope.$on('$destroy', function() {
        buttonOverlay.off('click', clickListener);
        diagramNode.off('mouseover mouseout');
      });
    }

    /**
     *
     * @param id (BPMN element id)
     * @param activity (activity associated with that id)
     */
    function addOverlayForSingleElement(id, activityInstances) {
      if (!overlaysNodes[id]) {
        overlaysNodes[id] = angular.element(template).hide();

        overlays.add(id, {
          position: {
            top: 0,
            right: 0
          },
          show: {
            minZoom: -Infinity,
            maxZoom: +Infinity
          },
          html: overlaysNodes[id]
        });

        addInteractions(overlaysNodes[id], id, activityInstances);
      }
    }

    var overlaysNodes = {};
    var overlays = control.getViewer().get('overlays');
    var TAB_NAME = 'called-process-instances-tab';

    processData.observe('activityIdToInstancesMap', function(activitiesMap) {
      getFlowNodes().map(function(id) {
        return !!activitiesMap[id] && activitiesMap[id].length > 0 && addOverlayForSingleElement(id, activitiesMap[id]);
      });
    });
  }
];
