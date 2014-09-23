define([
  'angular',
  'bpmn-js',
  'snap-svg',
  'text!./cam-tasklist-task-diagram.html'
], function(
  angular,
  Viewer,
  Snap,
  template
) {
  'use strict';

  // Dat ain't neat...
  window.Snap = Snap;


  return [
    'camAPI',
    '$timeout',
  function(
    camAPI,
    $timeout
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
        var viewer;
        var holder = element.find('.diagram-holder');

        scope.$watch('task', function() {
          scope.process = scope.task._embedded.processDefinition[0];
        });

        scope.$watch('process', function() {
          scope.drawDiagram();
        });

        scope.drawDiagram = function() {
          if (!scope.process) { return; }

          scope.loading = true;
          scope.rendering = true;

          holder.html('');
          if (viewer) {
            viewer.clear();
          }

          ProcessDefinition.xml(scope.process, function(err, xml) {
            scope.loading = false;
            if (err) { throw err; }

            viewer = new Viewer({
              container: holder
            });
            // holder.html('');
            // viewer.clear();

            // viewer.importXML(xml.bpmn20Xml, function(err) {
            //   scope.rendering = false;
            //   if (err) { throw err; }
            //   viewer.get('canvas').zoom('fit-viewport');
            // });

            angular.element('.bjs-container > a').remove();

            $timeout(function() {
              viewer.importXML(xml.bpmn20Xml, function(err) {
                if (err) { throw err; }

                $timeout(function() {
                  // var elSelector = '[data-element-id="'+ scope.task.taskDefinitionKey +'"]';
                  // var $el = element.find(elSelector);
                  // $el.addClass('highlight');
                  viewer.get('canvas').zoom('fit-viewport');
                  scope.rendering = false;
                }, 10);
              });
            }, 10);
          });
        };
      }
    };
  }];
});
