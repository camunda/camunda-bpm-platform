/* global define: false, console: false */
define([
  'angular',
  'jquery',
  'bpmn/Bpmn',
  'text!./processDiagram.html',
  'jquery-overscroll',
  'jquery-mousewheel'
], function(angular, $, Bpmn, template) {
  'use strict';
  /* jshint unused: false */
  var _unique = 0;
  function unique(prefix) {
    _unique++;
    return (prefix ? prefix +'_' : '') + _unique;
  }

  var DirectiveController = ['$scope', '$compile', 'Views', '$timeout',
                    function( $scope,   $compile,   Views,   $timeout) {

    $scope.overlayVars = { read: [ 'processData', 'bpmnElement' ] };
    $scope.overlayProviders = Views.getProviders({ component:  $scope.providerComponent });
    var overlay = '<div class="bpmn-overlay"><div view ng-repeat="overlayProvider in overlayProviders" provider="overlayProvider" vars="overlayVars"></div></div>';

    var bpmnElements,
        selection,
        scrollToBpmnElementId;

    $scope.$on('$destroy', function() {
      $scope.processDiagram = null;
      $scope.overlayProviders = null;
    });

    $scope.control = {};

    /**
     * If the process diagram changes, then the diagram will be rendered.
     */
    $scope.$watch('processDiagram', function(newValue) {
      if (newValue && newValue.$loaded !== false) {
        bpmnElements = newValue.bpmnElements;
        $scope.diagramXML = newValue.bpmn20Xml.bpmn20Xml;
      }
    });

    $scope.onLoad = function() {
      decorateDiagram($scope.processDiagram.bpmnElements);

      // update selection in case it has been provided earlier
      updateSelection(selection);

      // update scroll to in case it has been provided earlier
      scrollToBpmnElement(scrollToBpmnElementId);
    };

    $scope.onClick = function(element, $event) {
      if(bpmnElements[element.businessObject.id] && bpmnElements[element.businessObject.id].isSelectable) {
        $scope.onElementClick({id: element.businessObject.id, $event: $event});
      } else {
        $scope.onElementClick({id: null, $event: $event});
      }
    };

    /*------------------- Decorate diagram ---------------------*/

    function decorateDiagram(bpmnElements) {
      angular.forEach(bpmnElements, function (bpmnElement) {
        decorateBpmnElementWithOverlays(bpmnElement);
      });
    }

    function decorateBpmnElementWithOverlays(bpmnElement) {

      var elem = $scope.control.getElement(bpmnElement.id);

      if(elem) {
        var childScope = $scope.$new();

        childScope.bpmnElement = bpmnElement;

        var newOverlay = angular.element(overlay);

        newOverlay.css({
          width: elem.width,
          height: elem.height
        });

        $compile(newOverlay)(childScope);

         try {
          $scope.control.createBadge(bpmnElement.id, {
            html: newOverlay,
            position: {
              top: 0,
              left: 0
            }
          });
        } catch (exception) {
          // console.log('exception while creating badge for '+bpmnElement.id+':', exception);
        }
      }
    }

    /*------------------- Handle selected activity id---------------------*/

    $scope.$watch('selection.activityIds', function(newValue, oldValue) {
      updateSelection(newValue);
    });

    function updateSelection(newSelection) {
      if ($scope.control.isLoaded()) {
        if (selection) {
          angular.forEach(selection, function(elementId) {
            $scope.control.clearHighlight(elementId);
          });
        }

        if (newSelection) {
          angular.forEach(newSelection, function(elementId) {
            $scope.control.highlight(elementId);
          });
        }
      }

      $scope.$root.$emit('instance-diagram-selection-change', newSelection);

      selection = newSelection;
    }

    /*------------------- Handle scroll to bpmn element ---------------------*/

    $scope.$watch('selection.scrollToBpmnElement', function(newValue) {
      if (newValue) {
        scrollToBpmnElement(newValue);
      }
    });

    function scrollToBpmnElement(bpmnElementId) {
      if ($scope.control.isLoaded() && bpmnElementId) {
        scrollTo(bpmnElementId);
      }
      scrollToBpmnElementId = bpmnElementId;
    }

    function scrollTo(elementId) {
      $scope.control.scrollToElement(elementId);
    }

  }];

  var Directive = function ($compile, Views) {
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
      controller: DirectiveController,
      template: template
    };
  };

  Directive.$inject = [ '$compile', 'Views'];

  return Directive;
});
