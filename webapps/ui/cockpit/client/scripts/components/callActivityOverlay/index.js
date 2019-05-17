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
    'processDiagram',
    'PluginProcessInstanceResource',
    function(
      $scope,
      $timeout,
      $location,
      $translate,
      search,
      control,
      processData,
      processDiagram,
      PluginProcessInstanceResource
    ) {
      /**
       * @returns {Array} BPMN Elements that are flow nodes
       */
      function getFlowNodes() {
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
       * shows calledProcessInstances tab filtered by activityId
       * @param activities
       */
      function showCalledPInstances(activities) {
        var params = angular.copy(search());
        viewContext === 'history'
          ? (params.detailsTab = TAB_NAME)
          : (params.tab = TAB_NAME);
        search.updateSilently(params);

        $scope.processData.set('filter', {
          activityIds: [activities[0].activityId],
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

        var redirectToCalledPInstance = function(activityInstance) {
          var url =
            '/process-instance/' +
            activityInstance.calledProcessInstanceId +
            '/' +
            viewContext;
          $location.url(url);
        };

        var clickListener = function() {
          buttonOverlay.tooltip('hide');
          return activityInstances.length > 1
            ? applyFunction(showCalledPInstances, activityInstances)
            : applyFunction(redirectToCalledPInstance, activityInstances[0]);
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
          buttonOverlay.off('mouseover mouseout click');
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

          overlaysNodes[id].tooltip({
            container: 'body',
            title: $translate.instant(
              'PLUGIN_ACTIVITY_INSTANCE_SHOW_CALLED_PROCESS_INSTANCES'
            ),
            placement: 'top',
            animation: false
          });

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
      var elementRegistry = control.getViewer().get('elementRegistry');
      var TAB_NAME = 'called-process-instances-tab';
      var flowNodes = getFlowNodes();
      var callActivityToInstancesMap = {};

      /**
       * adds the callActivity overlay to each callActivity to a processInstance
       * @param callActivityToInstancesMap
       */
      var addOverlays = function(callActivityToInstancesMap) {
        Object.keys(callActivityToInstancesMap).map(function(id) {
          return (
            callActivityToInstancesMap[id][0].calledProcessInstanceId &&
            addOverlayForSingleElement(id, callActivityToInstancesMap[id])
          );
        });
      };

      /**
       * returns activityIdToInstancesMap but with only non empty callActivities
       * @param flowNodes (flowNodes of type CallActivity only)
       * @param activityIdToInstancesMap
       */
      var getCallActivitiesMap = function(flowNodes, activityIdToInstancesMap) {
        return flowNodes.reduce(function(map, id) {
          if (
            activityIdToInstancesMap[id] &&
            activityIdToInstancesMap[id].length > 0
          ) {
            map[id] = activityIdToInstancesMap[id];
          }
          return map;
        }, {});
      };

      if (viewContext === 'history') {
        flowNodes.length &&
          processData.observe('activityIdToInstancesMap', function(
            activityIdToInstancesMap
          ) {
            callActivityToInstancesMap = getCallActivitiesMap(
              flowNodes,
              activityIdToInstancesMap
            );
            addOverlays(callActivityToInstancesMap);
          });
      } else {
        flowNodes.length &&
          processData.observe(
            ['activityIdToInstancesMap', 'processInstance'],
            function(activityIdToInstancesMap, processInstance) {
              callActivityToInstancesMap = getCallActivitiesMap(
                flowNodes,
                activityIdToInstancesMap
              );

              // For each callActivity, add calledProcessInstanceId to the first activity instance
              //  this is done so that it can be used to redirect to the calledProcessInstance.
              PluginProcessInstanceResource.processInstances(
                {id: processInstance.id},
                function(calledPInstances) {
                  calledPInstances.forEach(function(calledPInstance) {
                    var instances =
                      callActivityToInstancesMap[
                        calledPInstance.callActivityId
                      ];
                    if (instances && instances.length) {
                      instances[0].calledProcessInstanceId = calledPInstance.id;
                    }
                  });
                  return addOverlays(callActivityToInstancesMap);
                }
              );
            }
          );
      }
    }
  ];
};
