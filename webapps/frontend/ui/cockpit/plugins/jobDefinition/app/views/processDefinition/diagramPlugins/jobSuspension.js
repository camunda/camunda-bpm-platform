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
var fs = require('fs');

var template = require('./jobSuspension.html')();

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView(
      'cockpit.processDefinition.diagram.plugin',
      {
        id: 'activity-instance-statistics-overlay',
        overlay: [
          '$scope',
          'control',
          'processData',
          'processDiagram',
          'camAPI',
          function($scope, control, processData, processDiagram, camAPI) {
            var viewer = control.getViewer();
            var overlays = viewer.get('overlays');
            var elementRegistry = viewer.get('elementRegistry');

            var overlaysNodes = {};

            $scope.$on(
              '$processDefinition.suspensionState.changed',
              function() {
                loadJobDefinitions();
              }
            );

            var processDefinition = null;

            var loadJobDefinitions = function(_processDefinition) {
              processDefinition = _processDefinition || processDefinition;

              if (!processDefinition) return;

              camAPI
                .resource('job-definition')
                .list({
                  processDefinitionId: processDefinition.id,
                  firstResult: 0,
                  maxResults: 2000
                })
                .then(function(jobDefinitions) {
                  elementRegistry.forEach(function(shape) {
                    var element =
                      processDiagram.bpmnElements[shape.businessObject.id];
                    var definitionsForElement = getElementDefinitions(
                      element,
                      jobDefinitions
                    );

                    if (definitionsForElement.length > 0) {
                      element.isSelectable = true;
                    }

                    function isSuspended() {
                      return definitionsForElement.some(function(definition) {
                        return definition.suspended;
                      });
                    }

                    $scope.$watch(isSuspended, function(suspended) {
                      var node = overlaysNodes[element.id];

                      if (!node && suspended) {
                        node = angular.element(template);

                        overlays.add(element.id, {
                          position: {
                            top: 0,
                            right: 0
                          },
                          show: {
                            minZoom: -Infinity,
                            maxZoom: +Infinity
                          },
                          html: node[0]
                        });

                        overlaysNodes[element.id] = node;
                      }

                      if (node) {
                        if (suspended) {
                          node.show();
                          node.tooltip({
                            container: 'body',
                            title: 'Suspended Job Definition',
                            placement: 'top'
                          });
                        } else {
                          node.hide();
                        }
                      }
                    });
                  });
                });
            };

            processData.observe('processDefinition', loadJobDefinitions);

            function getElementDefinitions(element, jobDefinitions) {
              return jobDefinitions.filter(function(definition) {
                return definition.activityId === element.id;
              });
            }
          }
        ]
      }
    );
  }
];
