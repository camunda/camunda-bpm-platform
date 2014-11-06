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
    '$timeout',
  function($timeout) {

    return {
      scope: {
        processDiagram: '='
      },

      template: template,

      link: function($scope, $element) {

        var viewer = new Viewer({
          container: $element.find('.diagram-holder'),
          width: '100%',
          height: '100%'
        });

        var processDiagram = null;
        var canvas = null;

        $scope.$watch('processDiagram', function(newValue) {
          if (newValue) {
            processDiagram = newValue;
            renderDiagram();
          }
        });

        function renderDiagram() {
          if (processDiagram && processDiagram.bpmn20xml) {

            viewer.importXML(processDiagram.bpmn20xml, function(err) {

              $scope.$apply(function() {

                if (err) {
                  $scope.error = err;
                  return;
                }

                canvas = viewer.get('canvas');

                resizeDiagram();

                highlightTask();

              });
            });
          }
        }

        function highlightTask() {
          if (canvas && processDiagram && processDiagram.task) {

            var taskDefinitionKey = processDiagram.task.taskDefinitionKey;

            if (taskDefinitionKey) {
              canvas.addMarker(taskDefinitionKey, 'highlight');
              $('[data-element-id="'+ taskDefinitionKey+ '"]>.djs-outline').attr({
                rx: '14px',
                ry: '14px'
              });
            }
          }
        }

        function resizeDiagram() {
          resizeContainer();
          zoom();
        }

        function resizeContainer() {
          var height = $win.height();
          var top = $element.offset().top + 50;

          if ((height + 400) > top) {
            $element.height(height - top);
          }
          else {
            $element.height(400);
          }
        }

        function zoom() {
          if (canvas) {
            canvas.zoom('fit-viewport');
          }
        }

        var timer = null;
        function handleResize() {
          $timeout.cancel(timer);
          timer = $timeout(function () {
            resizeDiagram();
          }, 500);
        }

        function applyResize() {
          $scope.$apply();
        }

        $scope.$watch(function () {
          return $element.width();
        }, function() {
          handleResize();
        });

        $scope.$watch(function () {
          return $element.height();
        }, function() {
          handleResize();
        });

        $scope.$watch(function() {
          return $win.height();
        }, function () {
          handleResize();
        });

        $win.on('resize', applyResize);

        $element.on('$destroy', function() {
          $win.off('resize', applyResize);
        });

      }
    };
  }];
});
