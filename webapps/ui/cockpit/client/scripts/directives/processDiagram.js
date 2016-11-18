'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/processDiagram.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

var DirectiveController = ['$scope', '$compile', 'Views', function( $scope,   $compile,   Views) {

  $scope.vars = { read: [ 'processData', 'bpmnElement', 'pageData', 'viewer' ] };

  $scope.overlayProviders = Views.getProviders({ component:  $scope.overlayProviderComponent });

  var overlay = '<div class="bpmn-overlay"><div view ng-repeat="overlayProvider in overlayProviders" provider="overlayProvider" vars="vars"></div></div>';
  var actions = '<ul><li view ng-repeat="actionProvider in actionProviders" provider="actionProvider" vars="vars"></li></ul>';

  var bpmnElements,
      selection;

  $scope.$on('$destroy', function() {
    $scope.processDiagram = null;
    $scope.overlayProviders = null;
  });

  $scope.control = {};

  $scope.$on('resize', function() {
    $scope.control.refreshZoom();
  });

  /**
  * If the process diagram changes, then the diagram will be rendered.
  */
  $scope.$watch('processDiagram', function(newValue) {
    if (newValue && newValue.$loaded !== false) {
      bpmnElements = newValue.bpmnElements;
      $scope.diagramData = newValue.bpmnDefinition;
    }
  });

  $scope.onLoad = function() {
    $scope.viewer = $scope.control.getViewer();
    decorateDiagram($scope.processDiagram.bpmnElements);

    if ($scope.actionProviderComponent) {
      addActions();
    }

// update selection in case it has been provided earlier
    updateSelection(selection);
  };

  var isElementSelectable = function(element) {
    return element.isSelectable || (
      $scope.selectAll &&
      element.$instanceOf('bpmn:FlowNode')
    );
  };

  $scope.onClick = function(element, $event) {
    safeApply(function() {
      if(bpmnElements[element.businessObject.id] && isElementSelectable(bpmnElements[element.businessObject.id])) {
        $scope.onElementClick({id: element.businessObject.id, $event: $event});
      } else {
        $scope.onElementClick({id: null, $event: $event});
      }
    });
  };

  function safeApply(fn) {
    if (!$scope.$$phase) {
      $scope.$apply(fn);
    } else {
      fn();
    }
  }

  $scope.onMouseEnter = function(element) {
    if(bpmnElements[element.businessObject.id] && isElementSelectable(bpmnElements[element.businessObject.id])) {
      $scope.control.getViewer().get('canvas').addMarker(element.businessObject.id, 'selectable');
      $scope.control.highlight(element.businessObject.id);
    }
  };

  $scope.onMouseLeave = function(element) {
    if(bpmnElements[element.businessObject.id] &&
    isElementSelectable(bpmnElements[element.businessObject.id]) &&
    (!selection || selection.indexOf(element.businessObject.id) === -1) &&
    (!selection || selection.indexOf(element.businessObject.id + '#multiInstanceBody') === -1)) {
      $scope.control.clearHighlight(element.businessObject.id);
    }
  };

/*------------------- Decorate diagram ---------------------*/

  function decorateDiagram(bpmnElements) {
    angular.forEach(bpmnElements, decorateBpmnElement);
  }

  function decorateBpmnElement(bpmnElement) {

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
        // do nothing
      }
    }
  }

/*------------------- Add actions ------------------------------------*/

  function addActions() {
    $scope.actionProviders = Views.getProviders({ component:  $scope.actionProviderComponent });
    var actionElement = angular.element(actions);
    var childScope = $scope.$new();
    $compile(actionElement)(childScope);
    $scope.control.addAction( {
      html : actionElement
    });
  }

/*------------------- Handle selected activity id---------------------*/

  $scope.$watch('selection.activityIds', function(newValue) {
    updateSelection(newValue);
  });

  function updateSelection(newSelection) {
    if ($scope.control.isLoaded()) {
      if (selection) {
        angular.forEach(selection, function(elementId) {
          if(elementId.indexOf('#multiInstanceBody') !== -1 &&
elementId.indexOf('#multiInstanceBody') === elementId.length - 18) {
            elementId = elementId.substr(0, elementId.length - 18);
          }
          if(bpmnElements[elementId]) {
            $scope.control.clearHighlight(elementId);
          }
        });
      }

      if (newSelection) {
        angular.forEach(newSelection, function(elementId) {
          if(elementId.indexOf('#multiInstanceBody') !== -1 &&
elementId.indexOf('#multiInstanceBody') === elementId.length - 18) {
            elementId = elementId.substr(0, elementId.length - 18);
          }
          if(bpmnElements[elementId]) {
            $scope.control.highlight(elementId);
          }
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
  }

  function scrollTo(elementId) {
    if(bpmnElements[elementId]) {
      $scope.control.scrollToElement(elementId);
    }
  }

}];

var Directive = function() {
  return {
    restrict: 'EAC',
    scope: {
      processDiagram: '=',
      processDiagramOverlay: '=',
      onElementClick: '&',
      selection: '=',
      processData: '=',
      pageData: '=',
      overlayProviderComponent: '@',
      actionProviderComponent: '@',
      selectAll: '&'
    },
    controller: DirectiveController,
    template: template,

    link: function($scope) {
      $scope.selectAll = $scope.$eval($scope.selectAll);
    }
  };
};

module.exports = Directive;
