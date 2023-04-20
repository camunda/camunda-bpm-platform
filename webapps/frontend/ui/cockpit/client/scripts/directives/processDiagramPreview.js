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

var template = fs.readFileSync(
  __dirname + '/processDiagramPreview.html',
  'utf8'
);

var angular = require('../../../../../camunda-commons-ui/vendor/angular');

module.exports = [
  'ProcessDefinitionResource',
  'debounce',
  function(ProcessDefinitionResource, debounce) {
    return {
      restrict: 'EAC',
      template: template,
      controller: [
        '$scope',
        'configuration',
        function($scope, configuration) {
          $scope.bpmnJsConf = configuration.getBpmnJs();
          $scope.control = {};
        }
      ],
      link: function(scope, element, attrs) {
        scope.$watch(attrs.processDefinitionId, function(processDefinitionId) {
          if (processDefinitionId) {
            // set the element id to processDiagram_*
            var elementId =
              'processDiagram_' + processDefinitionId.replace(/[.|:]/g, '_');
            element.attr('id', elementId);

            ProcessDefinitionResource.getBpmn20Xml({id: processDefinitionId})
              .$promise.then(function(response) {
                scope.diagramXML = response.bpmn20Xml;
                element.find('[cam-widget-bpmn-viewer]').css({
                  width: parseInt(element.parent().width(), 10),
                  height: element.parent().height()
                });

                var debouncedZoom = debounce(function() {
                  // Zoom is only correct after resetting twice.
                  // See: https://github.com/bpmn-io/diagram-js/issues/85

                  scope.control.resetZoom();
                  scope.control.resetZoom();
                }, 500);
                angular.element(window).on('resize', function() {
                  element.find('[cam-widget-bpmn-viewer]').css({
                    width: parseInt(element.parent().width(), 10),
                    height: element.parent().height()
                  });
                  debouncedZoom();
                });
              })
              .catch(angular.noop);
          }
        });
      }
    };
  }
];
