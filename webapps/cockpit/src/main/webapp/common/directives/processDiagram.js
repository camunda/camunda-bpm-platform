'use strict';

angular
  .module('cockpit.directive.process.diagram', ['cockpit.resource.process.definition.diagram'])
  .directive('processDiagram', function(ProcessDefinitionDiagramService) {
    return {
      restrict: 'A',
      replace : true,
      link: function(scope, element, attrs, $destroy) {
        if (!!scope.processDefinitionId) {
          var renderer = BpmnRenderer;

          ProcessDefinitionDiagramService.getBpmn20Xml(scope.processDefinitionId).then(
            function(data) {
              renderer.render(data.bpmn20Xml, element);
            }
          );

          scope.$on($destroy, function() {
            renderer = null;
          })
        }
      }
    };
  });