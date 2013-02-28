'use strict';

angular
  .module('cockpit.directive.process.diagram', ['cockpit.resource.process.definition.diagram'])
  .directive('processDiagram', function(ProcessDefinitionDiagramService, Debouncer) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs, $destroy) {
        if (!!scope.processDefinitionId) {
          var bpmnRenderer;

          // get dependencies
          require({
            baseUrl: "./",
            packages: [
              { name: "dojo", location: "assets/js/lib/dojo/dojo" },
              { name: "dojox", location: "assets/js/lib/dojo/dojox"},
              { name: "bpmn", location: "assets/js/lib/bpmn"}]
          });

          require(["bpmn/Bpmn", "dojo/domReady!"], function(Bpmn) {

            ProcessDefinitionDiagramService.getBpmn20Xml(scope.processDefinitionId).then(
              function(data) {
                bpmnRenderer = new Bpmn();
                bpmnRenderer.render(data.bpmn20Xml, {
                  diagramElement : "processDiagram",
                  overlayHtml : '<div></div>'
                });

                var highlightTasks = [ { task : 'reviewInvoice', count : '10' },
                                     { task : 'assignApprover', count : '10k' },
                                     { task : 'approveInvoice', count : '999' } ];
                angular.forEach(highlightTasks, function (currentTask) {
                  bpmnRenderer.annotate(currentTask.task, '<p class="currentActivityCount">' + currentTask.count + '</p>', ["currentActivity"])
                });

                var processId = '#saveInvoiceToSVN';
                // scroll to selected process if it is a collaboration
                $('#processDiagram').animate({
                  scrollTop: $(processId).offset().top,
                  scrollLeft: $(processId).offset().left
                }, 500);

              }
            );
          });

          var updateZoomLevel = function(zoomLevel) {
            Debouncer.debounce(function() {
              bpmnRenderer.zoom(parseFloat(zoomLevel));
            }, 1000)();
          };

          scope.$watch('zoomLevel', function(newValue) {
            if (newValue && bpmnRenderer) {
              updateZoomLevel(newValue);
            }
          });

          scope.$on($destroy, function() {
            bpmnRenderer = null;
          });

        }
      }
    };
  });