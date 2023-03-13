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
var instanceCount = require('../../common/diagramPlugins/instanceCount');

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView(
      'cockpit.processInstance.diagram.plugin',
      {
        id: 'activity-instance-statistics-overlay',
        overlay: [
          '$scope',
          'control',
          'processData',
          'processDiagram',
          'Loaders',
          '$filter',
          '$rootScope',
          '$translate',
          function(
            $scope,
            control,
            processData,
            processDiagram,
            Loaders,
            $filter,
            $rootScope,
            $translate
          ) {
            var callbacks = {
              observe: observe,
              getData: getInstancesCountsForElement,
              updateOverlayNodes: updateOverlayNodes,
              isActive: isActive
            };

            instanceCount(
              $scope,
              control,
              processData,
              processDiagram,
              Loaders,
              $rootScope,
              callbacks
            );

            function isActive(data) {
              return data.instanceCount > 0 || data.incidents;
            }

            function observe(callback) {
              processData.observe(
                ['activityIdToInstancesMap', 'activityIdToIncidentIdMap'],
                function(activityIdToInstancesMap, activityIdToIncidentIdMap) {
                  callback([
                    activityIdToInstancesMap,
                    activityIdToIncidentIdMap
                  ]);
                }
              );
            }

            function getInstancesCountsForElement(
              element,
              activityIdToInstancesMap,
              activityIdToIncidentsMap
            ) {
              var activityId = element.id;

              var instances = angular.copy(
                activityIdToInstancesMap[activityId] || []
              );
              var incidents = angular.copy(
                activityIdToIncidentsMap[activityId] || []
              );
              var instancesMI =
                activityIdToInstancesMap[activityId + '#multiInstanceBody'] ||
                [];
              var incidentsMI =
                activityIdToIncidentsMap[activityId + '#multiInstanceBody'] ||
                [];
              var multiInstance =
                activityIdToInstancesMap[activityId + '#multiInstanceBody'];

              return {
                instanceCount: getInstanceCount({
                  instances: instances,
                  instancesMI: instancesMI
                }),
                incidents: incidents.length || incidentsMI.length,
                multiInstance: multiInstance
              };
            }

            function getInstanceCount(data) {
              var count = 0;

              if (data.instances) {
                count += data.instances.length || 0;
              }

              if (data.instancesMI) {
                count +=
                  data.instancesMI.filter(function(instance) {
                    return instance.isTransitionInstance;
                  }).length || 0;
              }

              return count;
            }

            function updateOverlayNodes(nodes, data) {
              nodes.instancesNode.text(
                $filter('abbreviateNumber')(data.instanceCount)
              );

              nodes.incidentsNode.text(
                $filter('abbreviateNumber')(
                  data.incidents || data.childIncidents
                )
              );

              if (data.instanceCount <= 0) {
                nodes.instancesNode.hide();
              } else {
                nodes.instancesNode.show();
                nodes.instancesNode.tooltip({
                  container: 'body',
                  title: $translate.instant(
                    'PLUGIN_ACTIVITY_INSTANCE_RUNNING_ACTIVITY_INSTANCES'
                  ),
                  placement: 'top',
                  animation: false
                });
              }

              if (!data.incidents && !data.childIncidents) {
                nodes.incidentsNode.hide();
              } else {
                nodes.incidentsNode.show();
                nodes.incidentsNode.tooltip({
                  container: 'body',
                  title: $translate.instant(
                    'PLUGIN_ACTIVITY_INSTANCE_OPEN_INCIDENTS'
                  ),
                  placement: 'top',
                  animation: false
                });
              }
            }
          }
        ]
      }
    );
  }
];
