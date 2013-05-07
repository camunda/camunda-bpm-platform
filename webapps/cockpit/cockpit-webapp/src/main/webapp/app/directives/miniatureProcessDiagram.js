"use strict";

define([ "angular", "jquery", "bpmn/Bpmn", "dojo/domReady!" ], function(angular, $, Bpmn) {
  
  var module = angular.module("cockpit.directives");
  
  var Directive = function (ProcessDefinitionDiagramService) {
    return {
      restrict: 'A',
      scope: {
        processDefinitionId: '@processDefinitionId'
      },
      link: function(scope, element, attrs, $destroy) {
        
        var processDefinitionId = scope.processDefinitionId;
        var bpmnRenderer;
        
        var elementId = "processDiagram_" + processDefinitionId.replace(/:/g, "_");
        
        $(element).attr("id", elementId);
        
        ProcessDefinitionDiagramService.getBpmn20Xml(processDefinitionId).then(
            function(data) {
              bpmnRenderer = new Bpmn();
              bpmnRenderer.render(data.bpmn20Xml, {
                diagramElement : elementId,
                overlayHtml : '<span></span>',
                width : parseInt($(element).parent().css("min-width")),
                height : $(element).parent().height(),
              });
        });
        
        scope.$on($destroy, function() {
          bpmnRenderer = null;
        });
        
      }
    };
  };
  
  Directive.$inject = ["ProcessDefinitionDiagramService"];
  
  module
    .directive('miniatureProcessDiagram', Directive);
  
});