'use strict';

angular
  .module('cockpit.directive.process.diagram', [])
  .directive('processDiagram', function() {
    return {
      restrict: 'A',
      replace : true,
      link: function(scope, element, attrs, $destroy) {

        var renderer = BpmnRenderer;
        renderer.render(scope.processDiagramXml.bpmn20Xml, element);

        scope.$on($destroy, function() {
          renderer = null;
        })

      }
    };
  });