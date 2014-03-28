/* global ngDefine: false */
ngDefine('cockpit.directives', [ 'angular', 'bpmn/Bpmn' ], function(module, angular, Bpmn) {
  'use strict';

  var Directive = function (ProcessDefinitionResource) {
    return {
      restrict: 'EAC',
      template: '<span ng-hide="$loaded">' +
                '  <i class="icon-loading"></i> loading process diagram...' +
                '</span>',
      link: function(scope, element, attrs) {

        scope.$watch(attrs.processDefinitionId, function(processDefinitionId) {
          if (processDefinitionId) {
            // set the element id to processDiagram_*
            var elementId = 'processDiagram_' + processDefinitionId.replace(/[.|:]/g, '_');
            element.attr('id', elementId);

            ProcessDefinitionResource
            .getBpmn20Xml({ id : processDefinitionId })
              .$then(function(response) {
                var xml = response.data.bpmn20Xml;
                scope.$loaded = true;

                try {
                  new Bpmn().render(xml, {
                    diagramElement : element.attr('id'),
                    width : parseInt(element.parent().css('min-width')),
                    height : element.parent().height(),
                    skipOverlays: true
                  });
                } catch (exception) {
                  // console.log('Unable to render diagram for process definition ' + processDefinitionId + ', reason: ' + exception.message)
                  element.html('<div class="alert alert-error diagram-rendering-error">Unable to render process diagram.</div>');
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
