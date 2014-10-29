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

  return [function() {

    return {
      scope: {
        processDiagram: '='
      },

      template: template,

      link: function($scope, element) {

        var viewer = new Viewer({
          container: element.find('.diagram-holder')
        });

        $scope.rendering = false;

        var processDiagram = null;

        resizeContainer();

        $scope.$watch('processDiagram', function(newValue) {
          if (newValue) {
            processDiagram = newValue;
            renderDiagram();
          }
        });

        function renderDiagram() {
          if (processDiagram) {

            if (processDiagram.bpmn20xml) {
              $scope.rendering = true;
              
              viewer.importXML(processDiagram.bpmn20xml, function(err) {

                $scope.$apply(function() {
              
                  $scope.rendering = false;
                 
                  if (err) { 
                    $scope.error = err;
                  }

                  resizeContainer();

                  var canvas = viewer.get('canvas');

                  var taskDefinitionKey = (processDiagram.task || {}).taskDefinitionKey;
                  if (taskDefinitionKey) {
                    canvas.addMarker(taskDefinitionKey, 'highlight');
                  }
                  
                  canvas.zoom('fit-viewport');
                });
              });
            }
          }
        }

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

      }
    };
  }];
});
