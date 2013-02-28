'use strict';

angular
  .module('cockpit.directive.process.diagram', ['cockpit.resource.process.definition.diagram'])
  .directive('processDiagram', function(ProcessDefinitionDiagramService, ProcessDefinitionActivityStatisticsResource, Debouncer) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs, $destroy) {
        if (!!scope.processDefinitionId) {
          var containerElement = 'processDiagram';
          var bpmnRenderer;
          var currentActivityCssClass = 'currentActivity';
          var currentActivityCountCssClass = 'currentActivityCount';

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
                  diagramElement : containerElement,
                  overlayHtml : '<div></div>'
                });

                ProcessDefinitionActivityStatisticsResource.queryStatistics({ id : scope.processDefinitionId }).$then(function(result) {
                  scope.activityStatistics = getActivityStatisticsResult(result);
                  renderActivityStatistics(scope.activityStatistics, bpmnRenderer);
                });
              }
            );
          });

          var getActivityStatisticsResult = function(activityStatistics) {
            var activityStatisticsResult = [];

            angular.forEach(activityStatistics.data, function (currentActivityStatistic) {
              convertActivityStatisticNumber(currentActivityStatistic);
              activityStatisticsResult.push(angular.copy(currentActivityStatistic));
            });

            return activityStatisticsResult;
          }

          var convertActivityStatisticNumber = function(activityStatistic) {
            var instances = activityStatistic.instances;
            var numberLength = instances.toString().length;
            switch (numberLength) {
              case 4:
                // make it 1K
                activityStatistic.instances = instances / 1000 + 'K';
                break;
              case 5:
                // make it 10K
                activityStatistic.instances = instances / 1000 + 'K';
                break;
              case 6:
                // make it 100K
                activityStatistic.instances = instances / 1000 + 'K';
                break;
              case 7:
                // make it 1mn
                activityStatistic.instances = instances / 1000000 + 'mn';
                break;
              case 8:
                // make it 10mn
                activityStatistic.instances = instances / 1000000 + 'mn';
                break;
              case 9:
                // make it 100mn
                activityStatistic.instances = instances / 1000000 + 'mn';
                break;
              default:
                // leave it alone because it is under 1000
                break;
            }
          }

          var renderActivityStatistics = function(activityStatistics, renderer) {
            angular.forEach(activityStatistics, function (currentActivity) {
              renderer.annotate(currentActivity.id, '<p class="' + currentActivityCountCssClass + '">' + currentActivity.instances + '</p>', ["' + currentActivityCssClass + '"]);
            });
          };

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

          scope.$watch(function() { return scope.processDefinition }, function(processDefinition) {
            if (processDefinition) {
              // id to scroll to is the process definition key
              var processId = '#' + processDefinition.key;
              // scroll to selected process if it is a collaboration
              $('#' + containerElement).animate({
                scrollTop: $(processId).offset().top,
                scrollLeft: $(processId).offset().left
              }, 500);
            }
          });

          scope.$on($destroy, function() {
            bpmnRenderer = null;
          });

        }
      }
    };
  });