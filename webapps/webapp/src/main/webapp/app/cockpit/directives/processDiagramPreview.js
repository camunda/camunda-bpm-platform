'use strict';

ngDefine('cockpit.directives', [ 'angular', 'bpmn/Bpmn' ], function(module, angular, Bpmn) {
  
  var Directive = function (ProcessDefinitionResource) {
    return {
      restrict: 'EAC',
      link: function(scope, element, attrs) {
        
        scope.$watch(attrs['processDefinitionId'], function(processDefinitionId) {
          if (processDefinitionId) {
            
            // set the element id to processDiagram_*
            var elementId = 'processDiagram_' + processDefinitionId.replace(/:/g, '_');
            element.attr('id', elementId);
            
            ProcessDefinitionResource
            .getBpmn20Xml({ id : processDefinitionId })
              .$then(function(response) {
                var xml = response.data.bpmn20Xml;

                try {
                  new Bpmn().render(xml, {
                    diagramElement : element.attr('id'),
                    width : parseInt(element.parent().css("min-width")),
                    height : element.parent().height(),
                    skipOverlays: true
                  });
                } catch (exception) {
                  console.log('Unable to render diagram for process definition ' + processDefinitionId + ', reason: ' + exception.message)
                  element.html('<p style="text-align: center;margin-top: 100px;">Unable to render process diagram.</p>')
                }
              });
          }
        });
        
      }
    };
  };
  
  Directive.$inject = [ 'ProcessDefinitionResource' ];
  
  module
    .directive('processDiagramPreview', Directive);
  
});
