'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/processDiagramPreview.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = ['ProcessDefinitionResource', 'debounce', function(ProcessDefinitionResource, debounce) {
  return {
    restrict: 'EAC',
    template: template,
    controller: ['$scope', function($scope) {
      $scope.control = {};
    }],
    link: function(scope, element, attrs) {
      scope.$watch(attrs.processDefinitionId, function(processDefinitionId) {
        if (processDefinitionId) {
            // set the element id to processDiagram_*
          var elementId = 'processDiagram_' + processDefinitionId.replace(/[.|:]/g, '_');
          element.attr('id', elementId);

          ProcessDefinitionResource.getBpmn20Xml({ id : processDefinitionId }).$promise.then(function(response) {
            scope.diagramXML = response.bpmn20Xml;
            element.find('[cam-widget-bpmn-viewer]').css({
              width : parseInt(element.parent().width(), 10),
              height : element.parent().height()
            });

            var debouncedZoom = debounce(function() {
                // Zoom is only correct after resetting twice.
                // See: https://github.com/bpmn-io/diagram-js/issues/85

              scope.control.resetZoom();
              scope.control.resetZoom();
            }, 500);
            angular.element(window).on('resize', function() {
              element.find('[cam-widget-bpmn-viewer]').css({
                width : parseInt(element.parent().width(), 10),
                height : element.parent().height()
              });
              debouncedZoom();
            });

          });
        }
      });

    }
  };
}];
