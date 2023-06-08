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

var Viewer = require('cmmn-js/lib/NavigatedViewer');

var template = require('./cam-widget-cmmn-viewer.html?raw');

module.exports = [
  '$compile',
  '$location',
  '$rootScope',
  'search',
  'debounce',
  function($compile, $location, $rootScope, search, debounce) {
    return {
      scope: {
        diagramData: '=',
        control: '=?',
        disableNavigation: '&',
        onLoad: '&',
        onClick: '&',
        onMouseEnter: '&',
        onMouseLeave: '&'
      },

      template: template,

      link: function($scope, $element) {
        var definitions;

        $scope.grabbing = false;

        // parse boolean
        $scope.disableNavigation = $scope.$eval($scope.disableNavigation);

        // --- CONTROL FUNCTIONS ---
        $scope.control = $scope.control || {};

        $scope.control.highlight = function(element) {
          $scope.control.addMarker(element, 'highlight');

          var id = element.id || element;
          $element.find('[data-element-id="' + id + '"]>.djs-outline').attr({
            rx: '14px',
            ry: '14px'
          });
        };

        $scope.control.clearHighlight = function(id) {
          $scope.control.removeMarker(id, 'highlight');
        };

        $scope.control.isHighlighted = function(id) {
          return $scope.control.hasMarker(id, 'highlight');
        };

        $scope.control.addMarker = function(element, marker) {
          canvas.addMarker(element, marker);
        };

        $scope.control.removeMarker = function(element, marker) {
          canvas.removeMarker(element, marker);
        };

        $scope.control.hasMarker = function(element, marker) {
          return canvas.hasMarker(element, marker);
        };

        // config: text, tooltip, color, position
        $scope.control.createBadge = function(id, config) {
          var overlays = viewer.get('overlays');

          var htmlElement;
          if (config.html) {
            htmlElement = config.html;
          } else {
            htmlElement = document.createElement('span');
            if (config.color) {
              htmlElement.style['background-color'] = config.color;
            }
            if (config.tooltip) {
              htmlElement.setAttribute('tooltip', config.tooltip);
              htmlElement.setAttribute('tooltip-placement', 'top');
            }
            if (config.text) {
              htmlElement.appendChild(document.createTextNode(config.text));
            }
          }

          var overlayId = overlays.add(id, {
            position: config.position || {
              bottom: 0,
              right: 0
            },
            show: {
              minZoom: -Infinity,
              maxZoom: +Infinity
            },
            html: htmlElement
          });

          $compile(htmlElement)($scope);

          return overlayId;
        };

        // removes all badges for an element with a given id
        $scope.control.removeBadges = function(id) {
          viewer.get('overlays').remove({element: id});
        };

        // removes a single badge with a given id
        $scope.control.removeBadge = function(id) {
          viewer.get('overlays').remove(id);
        };

        $scope.control.getViewer = function() {
          return viewer;
        };

        $scope.control.scrollToElement = function(element) {
          var height, width, x, y;

          var elem = viewer.get('elementRegistry').get(element);
          var viewbox = canvas.viewbox();

          height = Math.max(viewbox.height, elem.height);
          width = Math.max(viewbox.width, elem.width);

          x = Math.min(
            Math.max(viewbox.x, elem.x - viewbox.width + elem.width),
            elem.x
          );
          y = Math.min(
            Math.max(viewbox.y, elem.y - viewbox.height + elem.height),
            elem.y
          );

          canvas.viewbox({
            x: x,
            y: y,
            width: width,
            height: height
          });
        };

        $scope.control.getElement = function(elementId) {
          return viewer.get('elementRegistry').get(elementId);
        };

        $scope.control.getElements = function(filter) {
          return viewer.get('elementRegistry').filter(filter);
        };

        $scope.loaded = false;
        $scope.control.isLoaded = function() {
          return $scope.loaded;
        };

        $scope.control.addAction = function(config) {
          var container = $element.find('.actions');
          var htmlElement = config.html;
          container.append(htmlElement);
          $compile(htmlElement)($scope);
        };

        $scope.control.addImage = function(image, x, y) {
          var addedImage = canvas._viewport.image(image, x, y);
          return addedImage;
        };

        var CmmnViewer = Viewer;
        if ($scope.disableNavigation) {
          CmmnViewer = Object.getPrototypeOf(Viewer.prototype).constructor;
        }
        var viewer = new CmmnViewer({
          container: $element[0].querySelector('.diagram-holder'),
          width: '100%',
          height: '100%',
          canvas: {
            deferUpdate: false
          }
        });

        // The following logic mirrors diagram-js to defer its update of the viewbox change.
        // We tell diagram-js to not defer the update (see above) and do it ourselves instead.
        // Only difference: We use a delay of 0. This causes the update to basically be propagated
        // immediately after the current execution is finished (instead of halting the execution
        // until the viewbox changes and all event listeners are executed). This results in a much
        // better performance while moving the diagram, but at a cost: In the interval between the
        // trigger of the viewbox change and the calculation of the event handlers in the debounced
        // execution, things like badges or migration arrows are at the wrong position; they feel
        // like they are "dragged behind". Therefore, we temporarily hide the overlays.

        // patch show and hide of overlays
        var originalShow = viewer
          .get('overlays')
          .show.bind(viewer.get('overlays'));
        viewer.get('overlays').show = function() {
          viewer.get('eventBus').fire('overlays.show');
          originalShow();
        };

        var originalHide = viewer
          .get('overlays')
          .hide.bind(viewer.get('overlays'));
        viewer.get('overlays').hide = function() {
          viewer.get('eventBus').fire('overlays.hide');
          originalHide();
        };

        var showAgain = debounce(function() {
          viewer.get('overlays').show();
        }, 300);

        var originalViewboxChanged = viewer
          .get('canvas')
          ._viewboxChanged.bind(viewer.get('canvas'));
        var debouncedOriginal = debounce(function() {
          originalViewboxChanged();
          viewer.get('overlays').hide();
          showAgain();
        }, 0);
        viewer.get('canvas')._viewboxChanged = function() {
          debouncedOriginal();
        };

        var diagramData = null;
        var canvas = null;

        $scope.$watch('diagramData', function(newValue) {
          if (newValue) {
            diagramData = newValue;
            renderDiagram();
          }
        });

        function renderDiagram() {
          if (diagramData) {
            $scope.loaded = false;

            var useDefinitions = typeof diagramData === 'object';

            var importFunction = (useDefinitions
              ? viewer.importDefinitions
              : viewer.importXML
            ).bind(viewer);

            importFunction(diagramData, function(err, warn) {
              var applyFunction = useDefinitions
                ? function(fn) {
                    fn();
                  }
                : $scope.$apply.bind($scope);

              applyFunction(function() {
                if (err) {
                  $scope.error = err;
                  return;
                }
                $scope.warn = warn;
                canvas = viewer.get('canvas');
                definitions = viewer._definitions;
                zoom();
                setupEventListeners();
                $scope.loaded = true;
                $scope.onLoad();
              });
            });
          }
        }

        function zoom() {
          if (canvas) {
            var viewbox = JSON.parse(
              ($location.search() || {}).viewbox || '{}'
            )[definitions.id];

            if (viewbox) {
              canvas.viewbox(viewbox);
            } else {
              canvas.zoom('fit-viewport', 'auto');
            }
          }
        }

        var mouseReleaseCallback = function() {
          $scope.grabbing = false;
          document.removeEventListener('mouseup', mouseReleaseCallback);
          $scope.$apply();
        };

        function setupEventListeners() {
          var eventBus = viewer.get('eventBus');
          eventBus.on('element.click', function(e) {
            // e.element = the model element
            // e.gfx = the graphical element
            $scope.onClick({element: e.element, $event: e.originalEvent});
          });
          eventBus.on('element.hover', function(e) {
            $scope.onMouseEnter({element: e.element, $event: e.originalEvent});
          });
          eventBus.on('element.out', function(e) {
            $scope.onMouseLeave({element: e.element, $event: e.originalEvent});
          });
          eventBus.on('element.mousedown', function() {
            $scope.grabbing = true;

            document.addEventListener('mouseup', mouseReleaseCallback);

            $scope.$apply();
          });
          eventBus.on(
            'canvas.viewbox.changed',
            debounce(function(e) {
              var viewbox = JSON.parse(
                ($location.search() || {}).viewbox || '{}'
              );

              viewbox[definitions.id] = {
                x: e.viewbox.x,
                y: e.viewbox.y,
                width: e.viewbox.width,
                height: e.viewbox.height
              };

              search.updateSilently({
                viewbox: JSON.stringify(viewbox)
              });

              var phase = $rootScope.$$phase;
              if (phase !== '$apply' && phase !== '$digest') {
                $scope.$apply(function() {
                  $location.replace();
                });
              } else {
                $location.replace();
              }
            }, 500)
          );
        }

        $scope.zoomIn = function() {
          viewer.get('zoomScroll').zoom(1, {
            x: $element[0].offsetWidth / 2,
            y: $element[0].offsetHeight / 2
          });
        };

        $scope.zoomOut = function() {
          viewer.get('zoomScroll').zoom(-1, {
            x: $element[0].offsetWidth / 2,
            y: $element[0].offsetHeight / 2
          });
        };

        $scope.resetZoom = function() {
          canvas.resized();
          canvas.zoom('fit-viewport', 'auto');
        };
        $scope.control.resetZoom = $scope.resetZoom;

        $scope.control.refreshZoom = function() {
          canvas.resized();
          canvas.zoom(canvas.zoom(), 'auto');
        };
      }
    };
  }
];
