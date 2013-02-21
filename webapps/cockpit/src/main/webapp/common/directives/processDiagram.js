'use strict';

angular
  .module('cockpit.directive.process.diagram', [])
  .directive('processDiagram', function() {
    return {
      restrict: 'E',
      template: '<div id="processDiagram""></div>',
      link: function(scope, element, attrs, $destroy) {

        var renderer = BpmnRenderer;
        renderer.render(scope.processDiagramXml.bpmn20_xml, element);

        scope.$on($destroy, function() {
          renderer = null;
        })

      }
    };
  });