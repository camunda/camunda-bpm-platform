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

var template = require('./processDiagram.html')();

var angular = require('../../../../../camunda-commons-ui/vendor/angular');

var DirectiveController = [
  '$scope',
  '$compile',
  '$injector',
  'Views',
  'configuration',
  function($scope, $compile, $injector, Views, configuration) {
    $scope.bpmnJsConf = configuration.getBpmnJs();

    $scope.vars = {read: ['processData', 'bpmnElement', 'pageData', 'viewer']};
    var diagramPlugins = $scope.diagramProviderComponent
      ? Views.getProviders({component: $scope.diagramProviderComponent})
      : [];

    $scope.overlayProviders = Views.getProviders({
      component: $scope.overlayProviderComponent
    });

    var overlay =
      '<div class="bpmn-overlay"><div view ng-repeat="overlayProvider in overlayProviders" provider="overlayProvider" vars="vars"></div></div>';
    var actions =
      '<div class="action"><div view ng-repeat="actionProvider in actionProviders" provider="actionProvider" vars="vars"></div></div>';

    var bpmnElements, selection;

    $scope.$on('$destroy', function() {
      $scope.processDiagram = null;
      $scope.overlayProviders = null;

      var control = $scope.control;
      var viewer = control.getViewer();
      var canvas = viewer.get('canvas');
      var elementRegistry = viewer.get('elementRegistry');

      elementRegistry.forEach(function(shape) {
        var bo = shape.businessObject;
        if (bo.$instanceOf('bpmn:FlowNode')) {
          canvas.removeMarker(bo.id, 'selectable');
          control.clearHighlight(bo.id);
        }
      });
    });

    $scope.control = {};

    $scope.$on('resize', function() {
      if ($scope.control.refreshZoom) {
        $scope.control.refreshZoom();
      }
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

    $scope.diagramLoaded = false;
    $scope.diagramEnabled = false;
    $scope.$watch('collapsed', function(collapsed) {
      $scope.diagramEnabled = $scope.diagramLoaded || !collapsed;
    });

    $scope.onLoad = function() {
      $scope.diagramLoaded = true;
      $scope.viewer = $scope.control.getViewer();
      decorateDiagram($scope.processDiagram.bpmnElements);

      if ($scope.actionProviderComponent) {
        addActions();
      }

      // update selection in case it has been provided earlier
      updateSelection(selection);

      //Apply diagram provider plugins
      diagramPlugins.forEach(function(plugin) {
        $injector.invoke(plugin.overlay, null, {
          control: $scope.control,
          processData: $scope.processData,
          processDiagram: $scope.processDiagram,
          pageData: $scope.pageData,
          $scope: $scope
        });
      });
    };

    var isElementSelectable = function(element) {
      return (
        element.isSelectable ||
        ($scope.selectAll && element.$instanceOf('bpmn:FlowNode'))
      );
    };

    $scope.onClick = function(element, $event) {
      safeApply(function() {
        // don't select invisible elements (process, collaboration, subprocess-plane)
        var isRoot = !element.parent;

        if (
          !isRoot &&
          bpmnElements[element.businessObject.id] &&
          isElementSelectable(bpmnElements[element.businessObject.id])
        ) {
          $scope.onElementClick({
            id: element.businessObject.id,
            $event: $event
          });
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
      if (
        bpmnElements[element.businessObject.id] &&
        isElementSelectable(bpmnElements[element.businessObject.id])
      ) {
        $scope.control
          .getViewer()
          .get('canvas')
          .addMarker(element.businessObject.id, 'selectable');
        $scope.control.highlight(element.businessObject.id);
      }
    };

    $scope.onMouseLeave = function(element) {
      if (
        bpmnElements[element.businessObject.id] &&
        isElementSelectable(bpmnElements[element.businessObject.id]) &&
        (!selection || selection.indexOf(element.businessObject.id) === -1) &&
        (!selection ||
          selection.indexOf(
            element.businessObject.id + '#multiInstanceBody'
          ) === -1)
      ) {
        $scope.control
          .getViewer()
          .get('canvas')
          .removeMarker(element.businessObject.id, 'selectable');
        $scope.control.clearHighlight(element.businessObject.id);
      }
    };

    /*------------------- Decorate diagram ---------------------*/

    function decorateDiagram(bpmnElements) {
      angular.forEach(bpmnElements, decorateBpmnElement);
    }

    function decorateBpmnElement(bpmnElement) {
      var elem = $scope.control.getElement(bpmnElement.id);

      if (elem && $scope.overlayProviders && $scope.overlayProviders.length) {
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
      $scope.actionProviders = Views.getProviders({
        component: $scope.actionProviderComponent
      });
      var actionElement = angular.element(actions);
      var childScope = $scope.$new();
      $compile(actionElement)(childScope);
      $scope.control.addAction({
        html: actionElement
      });
    }

    /*------------------- Handle selected activity id---------------------*/

    $scope.$watch('selection.activityIds', function(newValue) {
      updateSelection(newValue);
    });

    function updateSelection(newSelection) {
      if ($scope.control.isLoaded && $scope.control.isLoaded()) {
        if (selection) {
          angular.forEach(selection, function(elementId) {
            if (
              elementId.indexOf('#multiInstanceBody') !== -1 &&
              elementId.indexOf('#multiInstanceBody') === elementId.length - 18
            ) {
              elementId = elementId.substr(0, elementId.length - 18);
            }
            if (bpmnElements[elementId]) {
              $scope.control.clearHighlight(elementId);
            }
          });
        }

        if (newSelection) {
          angular.forEach(newSelection, function(elementId) {
            if (
              elementId.indexOf('#multiInstanceBody') !== -1 &&
              elementId.indexOf('#multiInstanceBody') === elementId.length - 18
            ) {
              elementId = elementId.substr(0, elementId.length - 18);
            }
            if (bpmnElements[elementId]) {
              $scope.control.highlight(elementId);
            }
          });
        }
      }

      $scope.$root.$emit('instance-diagram-selection-change', newSelection);

      selection = newSelection;
    }

    $scope.onRootChange = function() {
      var canvas = $scope.control.getViewer().get('canvas');
      var rootElement = canvas.getRootElement();
      var elementsOnRoot =
        selection?.filter(el => canvas.findRoot(el) === rootElement) || [];

      $scope.callbacks?.handleRootChange?.(elementsOnRoot, canvas);
    };

    /*------------------- Handle scroll to bpmn element ---------------------*/

    $scope.$watch('selection.scrollToBpmnElement', function(newValue) {
      if (newValue) {
        scrollToBpmnElement(newValue);
      }
    });

    function scrollToBpmnElement(bpmnElementId) {
      if (
        $scope.control.isLoaded &&
        $scope.control.isLoaded() &&
        bpmnElementId
      ) {
        scrollTo(bpmnElementId);
      }
    }

    function scrollTo(elementId) {
      if (bpmnElements[elementId]) {
        $scope.control.scrollToElement(elementId);
      }
    }
  }
];

var Directive = function() {
  return {
    restrict: 'EAC',
    scope: {
      processDiagram: '=',
      key: '@',
      processDiagramOverlay: '=',
      onElementClick: '&',
      selection: '=',
      callbacks: '=',
      processData: '=',
      pageData: '=',
      overlayProviderComponent: '@',
      actionProviderComponent: '@',
      diagramProviderComponent: '@',
      selectAll: '&',
      collapsed: '='
    },
    controller: DirectiveController,
    template: template,
    link: function($scope) {
      $scope.selectAll = $scope.$eval($scope.selectAll);
    }
  };
};

module.exports = Directive;
