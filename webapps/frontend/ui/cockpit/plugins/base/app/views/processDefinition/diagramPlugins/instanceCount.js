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

var instanceCount = require('../../common/diagramPlugins/instanceCount');

var template = require('./overlayAction.html?raw');

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView(
      'cockpit.processDefinition.diagram.action',
      {
        id: 'activity-instance-statistics-overlay',
        template: template,
        controller: [
          '$scope',
          'Loaders',
          'get',
          '$filter',
          '$rootScope',
          '$translate',
          'configuration',
          function(
            $scope,
            Loaders,
            get,
            $filter,
            $rootScope,
            $translate,
            configuration
          ) {
            $scope.enabled = configuration.getRuntimeActivityInstanceMetrics();

            $scope.toggle = $scope.enabled;
            $scope.isLoading = false;
            $scope.countOverlayIds = [];

            $scope.toggleOverlay = function() {
              $scope.toggle = !$scope.toggle;
              if ($scope.toggle) {
                showOverlays();
              } else {
                removeOverlays();
              }
            };

            function toggleIsLoading() {
              $scope.isLoading = !$scope.isLoading;
            }

            function showOverlays() {
              $scope.isLoading = true;

              var callbacks = {
                observe: observe,
                getData: getInstancesCountsForElement,
                updateOverlayNodes: updateOverlayNodes,
                isActive: isActive,
                toggleIsLoading: toggleIsLoading
              };

              instanceCount(
                $scope,
                $scope.viewer,
                $scope.processData,
                $scope.processDiagram,
                Loaders,
                $rootScope,
                callbacks
              );
            }

            function removeOverlays() {
              const overlays = $scope.viewer.get('overlays');
              $scope.countOverlayIds.forEach(id => {
                overlays.remove(id);
              });
              $scope.countOverlayIds = [];
            }

            $scope.processData.observe('processDiagram', function(
              processDiagram
            ) {
              $scope.processDiagram = processDiagram;
              $scope.toggle && showOverlays();
            });

            function observe(callback) {
              $scope.processData.observe(
                ['activityInstanceStatistics'],
                function(activityInstanceStatistics) {
                  callback([activityInstanceStatistics]);
                }
              );
            }

            function isActive(data) {
              return data.instances || data.incidents;
            }

            var getIncidentCount = function(incidents) {
              if (!incidents) {
                return 0;
              }

              return incidents.reduce(function(sum, incident) {
                return sum + incident.incidentCount;
              }, 0);
            };

            function getInstancesCountsForElement(
              element,
              activityInstanceStatistics
            ) {
              var stats = getStatsWithId(
                activityInstanceStatistics,
                element.id
              );
              var statsMi = getStatsWithId(
                activityInstanceStatistics,
                element.id + '#multiInstanceBody'
              );

              var statsIncidents = get(stats, ['incidents'], []);
              var statsMiIncidents = get(statsMi, ['incidents'], []);
              var incidents = statsIncidents.concat(statsMiIncidents);
              var incidentsCount = getIncidentCount(incidents);

              return {
                instances:
                  get(stats, ['instances'], 0) + get(statsMi, ['instances'], 0),
                incidents: incidentsCount
              };
            }

            function getStatsWithId(activityInstanceStatistics, id) {
              return activityInstanceStatistics.filter(function(entry) {
                return entry.id === id;
              })[0];
            }

            function setTextContent(node, number, childNumber) {
              node.text($filter('abbreviateNumber')(number || childNumber));
            }

            function updateOverlayNodes(nodes, data) {
              setTextContent(
                nodes.instancesNode,
                data.instances,
                data.childInstances && '...'
              );

              setTextContent(
                nodes.incidentsNode,
                data.incidents,
                data.childIncidents
              );

              if (!data.instances && !data.childInstances) {
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

              if (data.incidents || data.childIncidents) {
                nodes.incidentsNode.show();
                nodes.incidentsNode.tooltip({
                  container: 'body',
                  title: $translate.instant(
                    'PLUGIN_ACTIVITY_INSTANCE_OPEN_INCIDENTS'
                  ),
                  placement: 'top',
                  animation: false
                });
              } else {
                nodes.incidentsNode.hide();
              }
            }
          }
        ]
      }
    );
  }
];
