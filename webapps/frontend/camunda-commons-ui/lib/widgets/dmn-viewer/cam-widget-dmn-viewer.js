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

var migrateDiagram = require('@bpmn-io/dmn-migrate').migrateDiagram;

var angular = require('camunda-bpm-sdk-js/vendor/angular');
var Viewer = require('./lib/navigatedViewer').default;
var Modeler = require('camunda-dmn-js').CamundaPlatformModeler;
var changeDmnNamespace = require('../../util/change-dmn-namespace');

var template = require('./cam-widget-dmn-viewer.html?raw');

// This is only relevant when editMode===true,
// but we only want to rewrite the function once
var original = document.addEventListener;
document.addEventListener = function(...args) {
  const event = args[0];
  if (event === 'focusin') {
    return;
  }
  return original.apply(document, args);
};

module.exports = [
  '$window',
  function($window) {
    return {
      scope: {
        xml: '=',
        table: '@',
        width: '=',
        height: '=',
        control: '=?',
        editMode: '=',
        showDetails: '=',
        disableNavigation: '=',
        enableDrdNavigation: '=',
        disableLoader: '=',
        onLoad: '&',
        onClick: '&',
        onDblClick: '&'
      },
      template: template,
      link: function($scope, $element) {
        var canvas;
        var document = $window.document;

        $scope.isDrd = false;
        $scope.grabbing = false;

        // --- CONTROL FUNCTIONS ---
        $scope.control = $scope.control || {};

        $scope.control.getViewer = function() {
          return viewer;
        };

        $scope.control.getElement = function(elementId) {
          return viewer
            .getActiveViewer()
            .get('elementRegistry')
            .get(elementId);
        };

        $scope.loaded = false;
        $scope.control.isLoaded = function() {
          return $scope.loaded;
        };

        $scope.control.highlightRow = function(elementId, className) {
          var selector = '[data-row-id = ' + elementId + ']';
          angular
            .element(selector)
            .parent()
            .addClass(className);
        };

        $scope.control.highlightElement = function(id) {
          if (
            canvas &&
            viewer
              .getActiveViewer()
              .get('elementRegistry')
              .get(id)
          ) {
            canvas.addMarker(id, 'highlight');

            $element.find('[data-element-id="' + id + '"]>.djs-outline').attr({
              rx: '14px',
              ry: '14px'
            });
          }
        };

        $scope.control.clearAllElementsHighlight = function() {
          if (canvas) {
            var children = canvas.getRootElement().children;

            children.forEach(function(element) {
              var id = element.id;

              if (canvas.hasMarker(id, 'highlight')) {
                canvas.removeMarker(id, 'highlight');
              }
            });
          }
        };

        $scope.control.clearElementHighlight = function(id) {
          if (canvas) {
            canvas.removeMarker(id, 'highlight');
          }
        };

        $scope.control.isElementHighlighted = function(id) {
          if (canvas) {
            return canvas.hasMarker(id, 'highlight');
          }
        };

        $scope.control.getElements = function(filter) {
          if (canvas) {
            return viewer
              .getActiveViewer()
              .get('elementRegistry')
              .filter(filter);
          }
        };

        $scope.control.createBadge = function(id, config) {
          if (canvas) {
            addOverlay(id, config);
          }
        };

        $scope.control.resetZoom = resetZoom;

        $scope.control.refreshZoom = function() {
          canvas.resized();
          canvas.zoom(canvas.zoom(), 'auto');
        };

        function addOverlay(id, config) {
          var overlays = viewer.getActiveViewer().get('overlays');

          var overlayId = overlays.add(id, {
            position: config.position || {
              bottom: 10,
              right: 10
            },
            show: {
              minZoom: -Infinity,
              maxZoom: +Infinity
            },
            html: config.html
          });

          return overlayId;
        }

        var DmnViewer;

        if (!$scope.editMode) {
          DmnViewer = Viewer;
        } else {
          DmnViewer = Modeler;
        }

        var container = $element[0].querySelector('.table-holder');
        var viewer = new DmnViewer({
          container: container,
          width: $scope.width,
          height: $scope.height,
          hideDetails: !$scope.showDetails,
          tableViewOnly: $scope.table,
          drd: {
            drillDown: {
              enabled: $scope.enableDrdNavigation
            }
          }
        });

        var xml = null;

        $scope.$watch('xml', function(newValue) {
          if (newValue) {
            migrateDiagram(newValue).then(dmn13Xml => {
              xml = dmn13Xml;
              renderTable();
            });
          }
        });

        viewer.on('import.done', function() {
          viewer.getActiveViewer().on('element.click', function(e) {
            $scope.$apply(function() {
              $scope.onClick({
                element: e.element,
                $event: e.originalEvent
              });
            });
          });

          viewer.getActiveViewer().on('element.dblclick', function(e) {
            $scope.$apply(function() {
              $scope.onDblClick({
                element: e.element,
                $event: e.originalEvent
              });
            });
          });
        });

        var mouseReleaseCallback = $scope.$apply.bind($scope, function() {
          $scope.grabbing = false;
          document.removeEventListener('mouseup', mouseReleaseCallback);
        });

        viewer.on(
          'element.mousedown',
          $scope.$apply.bind($scope, function() {
            $scope.grabbing = true;

            document.addEventListener('mouseup', mouseReleaseCallback);
          })
        );

        $scope.zoomIn = function() {
          viewer
            .getActiveViewer()
            .get('zoomScroll')
            .zoom(1, {
              x: $element[0].offsetWidth / 2,
              y: $element[0].offsetHeight / 2
            });
        };

        $scope.zoomOut = function() {
          viewer
            .getActiveViewer()
            .get('zoomScroll')
            .zoom(-1, {
              x: $element[0].offsetWidth / 2,
              y: $element[0].offsetHeight / 2
            });
        };

        $scope.resetZoom = resetZoom;

        $window.addEventListener('resize', $scope.resetZoom);

        $scope.$on('destroy', function() {
          $window.removeEventListener('resize', $scope.resetZoom);
          document.removeEventListener('mouseup', mouseReleaseCallback);
        });

        function selectView() {
          if ($scope.table) {
            var isIndex = /^[0-9]+$/.test($scope.table);

            $scope.table = isIndex ? +$scope.table : $scope.table;

            var view = viewer.getViews().filter(function(view) {
              return (
                (angular.isString($scope.table) &&
                  view.element.id === $scope.table) ||
                view.element.name === $scope.table
              );
            })[0];

            viewer.open(view, function() {
              $scope.onLoad();
            });
          } else {
            // Table Name is not yet initalized, watch for the next change
            var unbind = $scope.$watch('table', function(newValue) {
              if (newValue) {
                unbind();
                selectView();
              }
            });
          }
        }

        function renderTable() {
          if (xml) {
            var correctedXML = changeDmnNamespace(xml);

            $scope.loaded = false;

            viewer.importXML(correctedXML, function(err) {
              const isTable = () => angular.isDefined($scope.table); // variable can be empty if table name is yet not initialized (e.g., pending request)
              $scope.isDrd =
                viewer.getDefinitions().drgElement.length > 1 && !isTable();

              if ($scope.isDrd) {
                canvas = viewer.getActiveViewer().get('canvas');
                canvas.zoom('fit-viewport', 'auto');

                $scope.control
                  .getElements(function(element) {
                    return element.type === 'dmn:Decision';
                  })
                  .forEach(function(element) {
                    canvas.addMarker(element.id, 'decision-element');
                  });
              }

              $scope.$apply(selectView);

              $scope.$apply(function() {
                if (err) {
                  $scope.error = err;
                  return;
                }

                if ($scope.isDrd) {
                  $scope.onLoad();
                }

                $scope.loaded = true;
              });
            });
          }
        }

        function resetZoom() {
          if (canvas) {
            canvas.resized();
            canvas.zoom('fit-viewport', 'auto');
          }
        }
      }
    };
  }
];
