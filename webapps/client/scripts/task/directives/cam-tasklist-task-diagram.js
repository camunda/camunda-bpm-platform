define([
  'angular',
  'bpmn-js',
  'text!./cam-tasklist-task-diagram.html'
], function(
  angular,
  Viewer,
  template
) {
  'use strict';
  var $ = angular.element;
  var $win = $(window);

  return [
    'camAPI',
  function(
    camAPI
  ) {
    var ProcessDefinition = camAPI.resource('process-definition');

    return {
      scope: {
        task: '='
      },

      template: template,

      link: function(scope, element) {
        scope.loading = false;
        scope.rendering = false;
        scope.process = null;

        var viewer = new Viewer({
          container: element.find('.diagram-holder')
        });


        function resizeContainer() {
          var height = $win.height();
          var top = element.offset().top + 50;

          if ((height + 400) > top) {
            element.height(height - top);
          }
          else {
            element.height(400);
          }
        }

        $win.on('resize', renderDiagram);
        element.on('$destroy', function() {
          $win.off('resize', renderDiagram);
        });


        scope.$watch('task', function(newV, oldV) {
          scope.process = scope.task._embedded.processDefinition[0];
          scope.drawDiagram();
        });


        function renderDiagram() {
          resizeContainer();

          var canvas = viewer.get('canvas');
          canvas.addMarker(scope.task.taskDefinitionKey, 'highlight');
          canvas.zoom('fit-viewport');

          scope.rendering = false;
        }

        scope.drawDiagram = function() {
          if (!scope.process || !element.width()) { return; }

          scope.loading = true;
          scope.rendering = true;

          ProcessDefinition.xml(scope.process, function(err, xml) {
            scope.loading = false;
            if (err) { throw err; }

            // yes we can
            $('.bjs-container > a').remove();

            viewer.importXML(xml.bpmn20Xml, function(err) {
              if (err) { throw err; }

              if (viewer.diagram) {
                renderDiagram();
              }
            });
          });
        };
      }
    };
  }];
});
