"use strict";

define([ "angular", "jquery", "bpmn/Bpmn", "dojo/domReady!", "bootstrap-slider/bootstrap-slider", "jqueryMousewheel" ], function(angular, $, Bpmn, Slider) {
  
  var module = angular.module("cockpit.directives");
  
  var Directive = function (ProcessDefinitionDiagramService, ProcessDefinitionActivityStatisticsResource, Debouncer) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs, $destroy) {
        if (!!scope.processDefinitionId) {
          var containerName = 'processDiagram';
          var container = $('#' + containerName);

          var bpmnRenderer;
//          var currentActivityCssClass = 'currentActivity';
          var currentActivityCountCssClass = 'currentActivityCount';

          // initial zoom level
          scope.zoomLevel = 1;

          container.mousewheel(function(event, delta) {
            scope.$apply(function() {
              scope.zoomLevel = calculateZoomLevel(delta);

            });
          });

          var calculateZoomLevel = function(zoomDelta) {
            var minZoomLevelMin = 0.1;
            var maxZoomLevelMax = 5;
            var zoomSteps = 10;

            var newZoomLevel = scope.zoomLevel + Math.round((zoomDelta * 100)/ zoomSteps) / 100;

            if (newZoomLevel > maxZoomLevelMax) {
              newZoomLevel = maxZoomLevelMax;
            } else if (newZoomLevel < minZoomLevelMin) {
              newZoomLevel = minZoomLevelMin;
            }

            return newZoomLevel;
          }

          scope.$watch('zoomLevel', function(newZoomLevel) {
            if (newZoomLevel && bpmnRenderer) {
              console.log("New ZoomLevel: " + newZoomLevel);
              bpmnRenderer.zoom(newZoomLevel);
            }
          });

          // initial dragging value
          var dragging;

          // register handlers
          container.mousedown(function(event) {
            if (event.button !== 0) {
              return;
            }

            dragging = {
              x: event.clientX,
              y: event.clientY
            };

            //$('#' + containerElement).addClass('drag');
            console.log("Start dragging: " + event);

            // bind mousemove
            container.mousemove(function(event) {
              if (!dragging) {
                return;
              }

              var delta = ({x: event.clientX - dragging.x, y: event.clientY - dragging.y});
              dragging.x = event.clientX;
              dragging.y = event.clientY;

              var currentLeft = parseInt(container.css('left'));
              var currentTop = parseInt(container.css('top'));

              if (!currentLeft || isNaN(currentLeft)) {
                currentLeft = 0;
              }
              if (!currentTop || isNaN(currentTop)) {
                currentTop = 0;
              }

              console.log("Delta (" + delta.x + "," + delta.y + ") - current (" + currentLeft + "," + currentTop + ")");

              var newLeft = currentLeft + delta.x + 'px';
              var newTop = currentTop + delta.y + 'px';

              console.log("New (" + newLeft + "," + newTop + ")");

              container.css('left', newLeft);
              container.css('top', newTop);
            })
          });

          container.mouseup(function(event) {
            if (!dragging) {
              return;
            }

            console.log("Stop dragging: " + event);

            dragging = null;
            //$('#' + containerElement).removeClass('drag');
            // unbind mousemove
            container.unbind('mousemove');
          });

          // on mousedown in svg start moving operation
          //

          var getActivityStatisticsResult = function(activityStatistics) {
            var activityStatisticsResult = [];

            angular.forEach(activityStatistics.data, function (currentActivityStatistic) {
              convertActivityStatisticNumber(currentActivityStatistic);
              activityStatisticsResult.push(angular.copy(currentActivityStatistic));
            });

            return activityStatisticsResult;
          };

          var convertActivityStatisticNumber = function(activityStatistic) {
            var instances = activityStatistic.instances;
            var numberLength = instances.toString().length;
            switch (numberLength) {
              case 4:
                // make it 1K
                activityStatistic.instances = Math.floor(instances / 1000) + 'K';
                break;
              case 5:
                // make it 10K
                activityStatistic.instances = Math.floor(instances / 1000) + 'K';
                break;
              case 6:
                // make it 100K
                activityStatistic.instances = Math.floor(instances / 1000) + 'K';
                break;
              case 7:
                // make it 1mn
                activityStatistic.instances = Math.floor(instances / 1000000) + 'mn';
                break;
              case 8:
                // make it 10mn
                activityStatistic.instances = Math.floor(instances / 1000000) + 'mn';
                break;
              case 9:
                // make it 100mn
                activityStatistic.instances = Math.floor(instances / 1000000) + 'mn';
                break;
              default:
                // leave it alone because it is under 1000
                break;
            }
          };

          var renderActivityStatistics = function(activityStatistics, renderer) {
            angular.forEach(activityStatistics, function (currentActivity) {
              renderer.annotate(currentActivity.id, '<p class="' + currentActivityCountCssClass + '">' + currentActivity.instances + '</p>');
            });
          };

          scope.$watch(function() { return scope.processDefinition; }, function(processDefinition) {
            if (processDefinition && processDefinition.$resolved) {

              ProcessDefinitionDiagramService.getBpmn20Xml(scope.processDefinitionId).then(
                function(data) {
                  bpmnRenderer = new Bpmn();
                  bpmnRenderer.render(data.bpmn20Xml, {
                    diagramElement : containerName,
                    overlayHtml : '<div></div>'
                  });

                  // id to scroll to is the process definition key
                  var processId = '#' + processDefinition.key;
                  // scroll to selected process if it is a collaboration

                  console.log("scrolling to point (" + $(processId).position().top + "," + $(processId).position().left + ")" );

                  container.animate({
                    scrollTop: $(processId).position().top,
                    scrollLeft: $(processId).position().left
                  }, 500);

                  ProcessDefinitionActivityStatisticsResource.queryStatistics({ id : scope.processDefinitionId }).$then(function(result) {
                    scope.activityStatistics = getActivityStatisticsResult(result);
                    renderActivityStatistics(scope.activityStatistics, bpmnRenderer);
                  });
                }
              );
            }
          });

          scope.$on($destroy, function() {
            bpmnRenderer = null;
          });

        }
      }
    };
  };
  
  Directive.$inject = ["ProcessDefinitionDiagramService", "ProcessDefinitionActivityStatisticsResource", "Debouncer"];
  
  module
    .directive('processDiagram', Directive);
  
});