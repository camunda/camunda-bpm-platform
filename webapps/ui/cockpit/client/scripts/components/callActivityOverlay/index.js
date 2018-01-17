var fs = require('fs');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/template.html', 'utf8');

module.exports = function(viewContext) {
  return [
    '$scope', '$timeout', '$location', 'search', 'control', 'processData', 'processDiagram', 'PluginProcessInstanceResource',
    function($scope, $timeout, $location, search, control, processData, processDiagram, PluginProcessInstanceResource) {
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
        viewContext === 'history' ?
          params.detailsTab = TAB_NAME :
          params.tab = TAB_NAME;
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

      var addOverlays = function(activitiesMap) {
        getFlowNodes().map(function(id) {
          return !!activitiesMap[id] && activitiesMap[id].length > 0 && addOverlayForSingleElement(id, activitiesMap[id]);
        });
      };

      if(viewContext === 'history') {
        processData.observe('activityIdToInstancesMap', addOverlays);
      } else {
        processData.observe(['activityIdToInstancesMap', 'processInstance'], function(activityIdToInstancesMap, processInstance) {
          // unaryCallActivitiesMap: activityIdToInstancesMap filtered with only call activities that have exactly 1 activityInstance
          var unaryCallActivitiesMap = Object.keys(activityIdToInstancesMap)
            .reduce(function(map, id) {
              if(activityIdToInstancesMap[id].length === 1) {
                map[id] = activityIdToInstancesMap[id];
              }
              return map;
            }, {});

          // if there are no unary call activities, then we don't need any extra information
          if(Object.keys(unaryCallActivitiesMap).length === 0) {
            return addOverlays(activityIdToInstancesMap);
          }

          // for each callActivity, add calledProcessInstanceId to the first activity instance
          // this is done so that it can be used to redirect to the calledProcessInstance
          PluginProcessInstanceResource
            .processInstances({ id: processInstance.id }, function(calledPInstances) {
              calledPInstances.forEach(function(calledPInstance) {
                unaryCallActivitiesMap[calledPInstance.callActivityId][0].calledProcessInstanceId = calledPInstance.id;
              });
              return addOverlays(unaryCallActivitiesMap);
            });
        });

      }

    }
  ];
};
