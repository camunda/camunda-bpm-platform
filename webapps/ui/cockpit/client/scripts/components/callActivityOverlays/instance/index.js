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

var angular = require('angular');
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
       * shows calledProcessInstances tab filtered by activityId
       * @param activities
       */
      function showCalledInstances(activities) {
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

      var redirectToCalledInstance = function(activityInstance) {
        var url =
          '/process-instance/' +
          activityInstance.calledProcessInstanceId +
          '/' +
          viewContext;
        $location.url(url);
      };

      var clickListener = function(activityInstances) {
        return activityInstances.length > 1
          ? $scope.$apply(() => showCalledInstances(activityInstances))
          : $scope.$apply(() => redirectToCalledInstance(activityInstances[0]));
      };

      var overlaysNodes = {};
      var overlays = control.getViewer().get('overlays');
      var elementRegistry = control.getViewer().get('elementRegistry');
      var TAB_NAME = 'called-process-instances-tab';
      var callActivityFlowNodes = getCallActivityFlowNodes(elementRegistry);
      var callActivityToInstancesMap = {};
      const tooltipText = $translate.instant(
        'PLUGIN_ACTIVITY_INSTANCE_SHOW_CALLED_PROCESS_INSTANCES'
      );

      /**
       * adds the callActivity overlay to each callActivity to a processInstance
       * @param callActivityToInstancesMap
       */
      var addOverlays = function(callActivityToInstancesMap) {
        Object.keys(callActivityToInstancesMap).map(function(id) {
          return (
            callActivityToInstancesMap[id][0].calledProcessInstanceId &&
            addOverlayForSingleElement({
              overlaysNodes,
              activityId: id,
              redirectionTarget: callActivityToInstancesMap[id],
              overlays,
              clickListener: clickListener,
              tooltipTitle: tooltipText,
              $scope,
              $timeout
            })
          );
        });
      };

      /**
       * returns activityIdToInstancesMap but with only non empty callActivities
       * @param flowNodes (callActivityFlowNodes of type CallActivity only)
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
        callActivityFlowNodes.length &&
          processData.observe('activityIdToInstancesMap', function(
            activityIdToInstancesMap
          ) {
            callActivityToInstancesMap = getCallActivitiesMap(
              callActivityFlowNodes,
              activityIdToInstancesMap
            );
            addOverlays(callActivityToInstancesMap);
          });
      } else {
        callActivityFlowNodes.length &&
          processData.observe(
            ['activityIdToInstancesMap', 'processInstance'],
            function(activityIdToInstancesMap, processInstance) {
              callActivityToInstancesMap = getCallActivitiesMap(
                callActivityFlowNodes,
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
