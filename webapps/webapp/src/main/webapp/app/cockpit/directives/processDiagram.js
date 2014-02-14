/* global ngDefine: false, console: false */
ngDefine('cockpit.directives', [
  'angular',
  'jquery',
  'bpmn/Bpmn',
  'jquery-overscroll',
  'jquery-mousewheel'
], function(module, angular, $, Bpmn) {
  'use strict';
  /* jshint unused: false */

  function DirectiveController($scope, $element, $attrs, $filter, $q, $window, $compile, Views) {

    var w = angular.element($window);

    $scope.overlayVars = { read: [ 'processData', 'bpmnElement' ] };
    $scope.overlayProviders = Views.getProviders({ component:  $scope.providerComponent });
    var overlay = '<div view ng-repeat="overlayProvider in overlayProviders" provider="overlayProvider" vars="overlayVars"></div>';

    var bpmnElements,
        selection,
        scrollToBpmnElementId;

    var activityHighligtClass = 'activity-highlight';
    var bpmnRenderer = null;
    var zoomLevel = 1;

    $scope.$on('$destroy', function() {
      bpmnRenderer = null;
      $scope.processDiagram = null;
      $scope.overlayProviders = null;
    });

    /*------------------- Rendering of process diagram ---------------------*/

    /**
     * If the process diagram changes, then the diagram will be rendered.
     */
    $scope.$watch('processDiagram', function(newValue) {
      if (newValue && newValue.$loaded !== false) {
        try {
          bpmnElements = newValue.bpmnElements;
          bpmnRenderer = new Bpmn();
          renderDiagram();
          decorateDiagram(bpmnElements);
          initializeScrollAndZoomFunctions();

          // update selection in case it has been provided earlier
          updateSelection(selection);

          // update scroll to in case it has been provided earlier
          scrollToBpmnElement(scrollToBpmnElementId);
        } catch (exception) {
          console.log('Unable to render diagram for process definition ' + $scope.processDiagram.processDefinition.id + ', reason: ' + exception.message);
          $element.html('<p style="text-align: center;margin-top: 100px;">Unable to render process diagram.</p>');
        }
      }
    });

    function renderDiagram() {

      // set the element id to processDiagram_*
      var elementId = 'processDiagram_' + $scope.processDiagram.processDefinition.id.replace(/[.|:]/g, '_');
      $element.attr('id', elementId);

      // clear innerHTML of element in case that the process diagram has changed
      // and the old one has been rendered.
      $element.empty();

      // set the render options
      $element.addClass('process-diagram');
      var options = {
        diagramElement : $element.attr('id')
      };

      // do the rendering
      bpmnRenderer.renderDiagram($scope.processDiagram.semantic, options);
    }

    /*------------------- Decorate diagram ---------------------*/

    function decorateDiagram(bpmnElements) {
      decorateProcessDiagramWithEventHandlers(bpmnElements);

      angular.forEach(bpmnElements, function (bpmnElement) {
        var activityId = bpmnElement.id,
            elem = bpmnRenderer.getOverlay(activityId);

        if (elem) {
          decorateBpmnElementWithOverlays(bpmnElement, elem);
          decorateBpmnElementWithEventHandlers(bpmnElement, elem);
        }

      });
    }

    function decorateProcessDiagramWithEventHandlers(bpmnElements) {

      var moved = false,
          mousedown = false;

      // register event handler on $element: mousedown, mousemove, mouseup
      $element
        // register mousedown event
        .mousedown(function() {
          mousedown = true;

          // indicates the timestamp, when
          // the mousemove event was
          // triggered.
          var mousemoveTimestamp = 0;

          // The mousemove event will be bind to the
          // $element only if a mousedown event happened
          // before.
          $element.mousemove(function($event) {
            var now = $event.timeStamp;
            // mousemoveTimestamp === 0 means,
            // that the mousemove event was triggered
            // but no mousemovement happened (this
            // is a workaround for chrome.)
            if (mousemoveTimestamp !== 0) {
              moved = true;
            }
            mousemoveTimestamp = now;
          });
        })

        // register mouseup event
        .mouseup(function($event) {
          var targetId = $($event.target).attr('data-activity-id'),
              bpmnElement = bpmnElements[targetId],
              ctrlKey = $event.ctrlKey;

          if (!ctrlKey) {

            if (!moved && mousedown && (!bpmnElement || !bpmnElement.isSelectable)) {
              // if the mouse have not moved, a mousedown happend and the bpmnElement is null
              // or is not selectable, then you have to deselect the current selection.
              if ($scope.onElementClick) {
                $scope.onElementClick({id: null, $event: $event});
                $scope.$apply();
              }
            }
          }

          // unbind the mousemove event
          $element.unbind('mousemove');

          // always reset the values
          moved = false;
          mousedown = false;
        });
    }

    function decorateBpmnElementWithOverlays(bpmnElement, htmlElement) {
      var childScope = $scope.$new();

      childScope.bpmnElement = bpmnElement;

      var newOverlay = angular.element(overlay);

      $compile(newOverlay)(childScope);
      htmlElement.html(newOverlay);
    }

    function decorateBpmnElementWithEventHandlers(bpmnElement, htmlElement) {
      var activityId = bpmnElement.id;
      $(htmlElement)

        // register click
        .click(activityId, function ($event) {
          if (bpmnElement.isSelectable) {
            $scope.onElementClick({
              id: $event.data, $event: $event
            });
            $scope.$apply();
          }
        })

        // mouseover
        .mouseover(activityId, function($event) {
          if (!bpmnElement.isSelected && bpmnElement.isSelectable) {

            // add css class to highlight activity
            bpmnRenderer.annotation($event.data).addClasses([ activityHighligtClass ]);
          }
        })

        // mouseout
        .mouseout(activityId, function($event){
          if (!bpmnElement.isSelected && bpmnElement.isSelectable) {
            // remove css class to highlight activity
            bpmnRenderer.annotation($event.data).removeClasses([ activityHighligtClass ]);
          }
        });
    }

    /*------------------- Handle scroll and zoom ---------------------*/


    $scope.$watch(function() { return zoomLevel; }, function(newZoomLevel) {
      if (!!newZoomLevel && !!bpmnRenderer) {
        zoom(newZoomLevel);
      }
    });

    function initializeScrollAndZoomFunctions() {
      zoom(zoomLevel);

      $element.mousewheel(function($event, delta) {
        $event.preventDefault();
        $scope.$apply(function() {
          zoomLevel = calculateZoomLevel(delta);
        });
      });
    }

    function overscroll() {
      $element.overscroll({captureWheel:false});
    }

    function removeOverscroll() {
      $element.removeOverscroll();
    }

    function zoom(zoomFactor) {
      removeOverscroll();
      bpmnRenderer.zoom(zoomFactor);
      overscroll();
    }

    function calculateZoomLevel (delta) {
      var minZoomLevelMin = 0.1;
      var maxZoomLevelMax = 5;
      var zoomSteps = 10;

      var newZoomLevel = zoomLevel + Math.round((delta * 100)/ zoomSteps) / 100;

      if (newZoomLevel > maxZoomLevelMax) {
        newZoomLevel = maxZoomLevelMax;
      } else if (newZoomLevel < minZoomLevelMin) {
        newZoomLevel = minZoomLevelMin;
      }

      return newZoomLevel;
    }

    /*------------------- Handle window resize ---------------------*/

    w.bind('resize', function () {
      $scope.$apply();
    });

    $scope.$watch(function () {
      return $element.width();
    }, function(newValue, oldValue) {
      if (bpmnRenderer) {
        zoom(zoomLevel);
      }
    });

    $scope.$watch(function () {
      return $element.height();
    }, function(newValue, oldValue) {
      if (bpmnRenderer) {
        zoom(zoomLevel);
      }
    });

    $scope.$on('resize', function () {
      $scope.$apply();
    });

    /*------------------- Handle selected activity id---------------------*/

    $scope.$watch('selection.activityIds', function(newValue, oldValue) {
      updateSelection(newValue);
    });

    function updateSelection(newSelection) {
      if (bpmnElements) {
        if (selection) {
          angular.forEach(selection, function(elementId) {
            var bpmnElement = bpmnElements[elementId];
            deselectActivity(bpmnElement);
          });
        }

        if (newSelection) {
          angular.forEach(newSelection, function(elementId) {
            var bpmnElement = bpmnElements[elementId];
            selectActivity(bpmnElement);
          });
        }
      }

      selection = newSelection;
    }

    function selectActivity(bpmnElement) {
      if (bpmnElement) {
        bpmnElement.isSelected = true;
        try {
          bpmnRenderer.annotation(bpmnElement.id).addClasses([ activityHighligtClass ]);
        } catch (error) {
          console.log('Could not add css class ' + activityHighligtClass + ' on element ' + bpmnElement.id + ': ' + error.message);
        }
      }
    }

    function deselectActivity(bpmnElement) {
      if (bpmnElement) {
        bpmnElement.isSelected = false;
        try {
          bpmnRenderer.annotation(bpmnElement.id).removeClasses([ activityHighligtClass ]);
        } catch (error) {
          console.log('Could not remove css class ' + activityHighligtClass + ' on element ' + bpmnElement.id + ': ' + error.message);
        }
      }
    }

    /*------------------- Handle scroll to bpmn element ---------------------*/

    $scope.$watch('selection.scrollToBpmnElement', function(newValue) {
      if (newValue) {
        scrollToBpmnElement(newValue);
      }
    });

    function scrollToBpmnElement(bpmnElementId) {
      if (bpmnElements) {
        var bpmnElement = bpmnElements[bpmnElementId];
        if (bpmnElement) {
          scrollTo(bpmnElement);
        }
      }
      scrollToBpmnElementId = bpmnElementId;
    }

    function scrollTo(element) {
      // parent size
      var parentElementHeight = $element.height();
      var parentElementWidth = $element.width();

      // get the bpmn element to scroll to
      var bpmnElement = bpmnRenderer.getOverlay(element.id);

      // get the height and width of the bpmn element
      var bpmnElementHeight = bpmnElement.height();
      var bpmnElementWidth = bpmnElement.width();

      // get the top and left position of the bpmn element
      var bpmnElementTop = parseInt(bpmnElement.css('top'));
      var bpmnElementLeft = parseInt(bpmnElement.css('left'));

      var scrollTop = (bpmnElementTop +  (bpmnElementHeight/2)) - parentElementHeight/2;
      var scrollLeft = (bpmnElementLeft +  (bpmnElementWidth/2)) - parentElementWidth/2;

      $element.animate({
        scrollTop: scrollTop,
        scrollLeft: scrollLeft
      });
    }

    this.getRenderer = function () {
      return bpmnRenderer;
    };

  }

  var Directive = function ($window, $compile, Views) {
    return {
      restrict: 'EAC',
      scope: {
        processDiagram: '=',
        processDiagramOverlay: '=',
        onElementClick: '&',
        selection: '=',
        processData: '=',
        providerComponent: '@'
      },
      controller: DirectiveController
    };
  };

  Directive.$inject = [ '$window', '$compile', 'Views'];

  module
    .directive('processDiagram', Directive);

});
