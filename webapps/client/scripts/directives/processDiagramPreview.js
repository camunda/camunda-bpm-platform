define(['text!./processDiagramPreview.html'], function(template) {
  'use strict';

  return ['ProcessDefinitionResource', function (ProcessDefinitionResource) {
    return {
      restrict: 'EAC',
      template: template,
      link: function(scope, element, attrs) {
        scope.$watch(attrs.processDefinitionId, function(processDefinitionId) {
          if (processDefinitionId) {
            // set the element id to processDiagram_*
            var elementId = 'processDiagram_' + processDefinitionId.replace(/[.|:]/g, '_');
            element.attr('id', elementId);

            ProcessDefinitionResource.getBpmn20Xml({ id : processDefinitionId }).$promise.then(function(response) {
              scope.diagramXML = response.bpmn20Xml;
              element.find('[cam-widget-bpmn-viewer]').css({
                width : parseInt(element.parent().width(), 10),
                height : element.parent().height(),
              });

            });
          }
        });

      }
    };
  }];
});
