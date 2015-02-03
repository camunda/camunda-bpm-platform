define(['bpmn/Bpmn'], function(Bpmn) {
  'use strict';

  var Directive = function (ProcessDefinitionResource) {
    return {
      restrict: 'EAC',
      template: '<span ng-hide="$loaded">' +
                '  <i class="glyphicon glyphicon-loading"></i> loading process diagram...' +
                '</span>',
      link: function(scope, element, attrs) {
        scope.$watch(attrs.processDefinitionId, function(processDefinitionId) {
          if (processDefinitionId) {
            // set the element id to processDiagram_*
            var elementId = 'processDiagram_' + processDefinitionId.replace(/[.|:]/g, '_');
            element.attr('id', elementId);

            ProcessDefinitionResource.getBpmn20Xml({ id : processDefinitionId }).$promise.then(function(response) {
              var xml = response.bpmn20Xml;
              scope.$loaded = true;

              try {
                new Bpmn().render(xml, {
                  diagramElement : element.attr('id'),
                  width : parseInt(element.parent().width(), 10),
                  height : element.parent().height(),
                  skipOverlays: true
                });
              }
              catch (exception) {
                element.html('<div class="alert alert-danger alert-error diagram-rendering-error">Unable to render process diagram.</div>');
              }
            });
          }
        });

      }
    };
  };

  Directive.$inject = [ 'ProcessDefinitionResource' ];

  return Directive;
});
