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
        var process;
        var viewer = new Viewer({
          container: element.find('.diagram-holder')
        });


        scope.loading = false;
        scope.rendering = false;


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


        function renderDiagram() {
          if (!process._xml) { return; }
          viewer.importXML(process._xml.bpmn20Xml, function(err) {
            if (err) { throw err; }

            resizeContainer();

            var canvas = viewer.get('canvas');
            canvas.addMarker(scope.task.taskDefinitionKey, 'highlight');
            canvas.zoom('fit-viewport');

            scope.rendering = false;
          });
        }


        scope.drawDiagram = function() {
          if (!process) { return; }
          if (!scope.$root.authentication) {
            throw new Error('Not authenticated');
          }

          scope.rendering = true;

          if (process._xml) {
            return renderDiagram();
          }

          scope.loading = true;
          ProcessDefinition.xml(process, function(err, xml) {
            scope.loading = false;
            if(err) {
              scope.error = err;
            } else {
              scope.error = null;
              process._xml = xml;
              renderDiagram();
            }
          });
        };

        scope.$on('tasklist.task.tab', function(evt, tabName) {
          if (tabName === 'diagram') {
            scope.drawDiagram();
          }
        });

        $win.on('resize', renderDiagram);
        element.on('$destroy', function() {
          $win.off('resize', renderDiagram);
        });


        scope.$watch('task', function(newV, oldV) {
          process = scope.task._embedded.processDefinition[0];
        });
      }
    };
  }];
});
