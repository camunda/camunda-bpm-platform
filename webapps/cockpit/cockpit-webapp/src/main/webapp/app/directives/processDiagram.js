'use strict';

ngDefine('cockpit.directives', [
    'angular',
    'jquery',
    'bpmn/Bpmn',
    'bootstrap-slider',
    'jquery-overscroll',
    'jquery-mousewheel',
    'dojo/domReady!'
  ], function(module, angular, $, Bpmn) {

  function DirectiveController($scope, $element, $attrs, $filter, ProcessDefinitionResource) {

    var activityStatistics = null;
    var activityInstances = null;
    var activitiesWithIncidents = null;
    
    var bpmnRenderer = null;
    var miniature = $scope.$eval($attrs['miniature']);
    var zoomLevel = null;

    $scope.$watch($attrs['processDefinitionId'], function (newValue) {
      if (newValue) {
        loadProcessDiagram(newValue);
      }
    });

    $scope.$watch(function() { return bpmnRenderer; }, function(newValue) {
      annotate();
    });

    $scope.$on('$destroy', function() {
      bpmnRenderer = null;
    });

    $scope.$watch(function() { return zoomLevel; }, function(newZoomLevel) {
      if (!!newZoomLevel && !!bpmnRenderer) {
        removeOverscroll();
        bpmnRenderer.zoom(newZoomLevel);
        overscroll();
      }
    });

    function loadProcessDiagram(processDefinitionId) {
      // set id of element
      var elementId = 'processDiagram_' + processDefinitionId.replace(/:/g, '_');
      $element.attr('id', elementId);

      // clear innerHTML of element
      $element.empty();

      // get the bpmn20xml
      ProcessDefinitionResource.getBpmn20Xml({
          id: processDefinitionId
        })
      .$then(
          function(data) {
            if (miniature && miniature === true) {
              renderMiniatureProcessDiagram(data.data.bpmn20Xml);
            } else {
              renderProcessDiagram(data.data.bpmn20Xml);
            }
          }
      );
    }

    function renderProcessDiagram (bpmn20Xml) {

      $element.addClass('process-diagram');

      try {
        bpmnRenderer = new Bpmn();
        bpmnRenderer.render(bpmn20Xml, {
          diagramElement : $element.attr('id')
        });

        zoomLevel = 1;

        $element.mousewheel(function(event, delta) {
          $scope.$apply(function() {
            zoomLevel = calculateZoomLevel(delta);
          });
        });
      } catch (err) {
        // clear innerHTML of element
        $element.empty();
        console.log('Could not render process diagram: ' + err.message);
        // TODO: Create a hint that the diagram could not be rendered.
      }
    }

    function renderMiniatureProcessDiagram (bpmn20Xml) {
      try {
        bpmnRenderer = new Bpmn();
        bpmnRenderer.render(bpmn20Xml, {
          diagramElement : $element.attr('id'),
          width : parseInt($element.parent().css("min-width")),
          height : $element.parent().height(),
        });
      } catch (err) {
        // clear innerHTML of element
        $element.empty();
        console.log('Could not render process diagram: ' + err.message);
        // TODO: Create a hint that the diagram could not be rendered.
      }
    }

    function overscroll() {
      $element.overscroll({captureWheel:false});
    }

    function removeOverscroll() {
      $element.removeOverscroll();
    }

    function calculateZoomLevel (delta) {
      var minZoomLevelMin = 0.1;
      var maxZoomLevelMax = 5;
      var zoomSteps = 10;

      var newZoomLevel = zoomLevel + Math.round((delta * 100)/ zoomSteps) / 100;

      if (newZoomLevel > maxZoomLevelMax) {
        newZoomLevel = maxZoomLevelMax;
      } else if (newZoomLevel < minZoomLevelMin) {
        newZoomLevel = minZoomLevelMin;
      }

      return newZoomLevel;
    };

    function annotate() {
      if (bpmnRenderer) {

        if (activityStatistics) {
          doAnnotateWithActivityStatistics(activityStatistics);
          // set to null, so that the number will not set twice
          activityStatistics = null;
        } else if (activityInstances) {
          doAnnotateWithActivityInstances(activityInstances);
          // set to null, so that the number will not set twice
          activityInstances = null;
        }
        if (activitiesWithIncidents) {
          doAnnotateWithIncidents(activitiesWithIncidents);
          // set to null, so that the incidents will not set twice
          activitiesWithIncidents = null;
        }
      }
    }

    function doAnnotateWithActivityStatistics(activityStaticstics) {
      angular.forEach(activityStatistics, function (currentActivityStatistics) {
        doAnnotate(currentActivityStatistics.id, currentActivityStatistics.instances);
      });
    }

    function doAnnotateWithActivityInstances(activityInstances) {
      var result = [];
      aggregateActivityInstances(activityInstances, result);

      for (var key in result) {
        var mappings = result[key];
        doAnnotate(key, mappings.length);
      }

    }

    function aggregateActivityInstances(instance, map) {
      
      var children = instance.childActivityInstances;
      for (var i = 0; i < children.length; i++) {
        var child = children[i];
        aggregateActivityInstances(child, map);
        
        var mappings = map[child.activityId];
        if (!mappings) {
          mappings = [];
          map[child.activityId] = mappings;
        }
        mappings.push(child);
      }

      var transitions = instance.childTransitionInstances;
      for (var i = 0; i < transitions.length; i++) {
        var transition = transitions[i];
        
        var mappings = map[transition.targetActivityId];
        if (!mappings) {
          mappings = [];
          map[transition.targetActivityId] = mappings;
        }
        mappings.push(transition);
      }
    }

    function doAnnotateWithIncidents(activitiesWithIncidents) {
      angular.forEach(activitiesWithIncidents, function (activity) {
        if (activity.incidents && activity.incidents.length > 0) {
          executeAnnotation(activity.id, '<p class="badge badge-important">!</p>');
        }
      });
    }
    
    function doAnnotate(activityId, count) {
      var shortenNumberFilter = $filter('shortenNumber');
      executeAnnotation(activityId, '<p class="badge">' + shortenNumberFilter(count) + '</p>');
    }
    
    function executeAnnotation(activityId, innerHtml) {
      // Select corresponding div for activityId
      var activity = $('#' + $element.attr('id') + ' > #' + activityId);
      if (activity) {
        // get innerHTML of activity (i.e. div)
        var html = activity.html();
        if (html) {
          // If there exists an innerHTML then get the div as element
          // and append the the assigned 'innerHtml'
          var badgeElement = $("#" + activityId + ' > .badgePosition');
          badgeElement.append(innerHtml);
        } else {
          // If there does not exist an innerHTML then add a new div to the 
          // activity div via bpmnRenderer.
          bpmnRenderer.annotation(activityId).addDiv(innerHtml, ['badgePosition']);
        }
      }
    }

    this.getRenderer = function () {
      return bpmnRenderer;
    };

    this.annotateWithActivityStatistics = function (statistics) {
      activityStatistics = statistics;
      annotate();
    };

    this.annotateWithActivityInstances = function (instances) {
      activityInstances = instances;
      annotate();
    };
    this.annotateWithIncidents = function (activities) {
      activitiesWithIncidents = activities;
      annotate();
    };
    
  }

  var Directive = function (ProcessDefinitionResource) {
    return {
      restrict: 'EAC',
      controller: DirectiveController
    };
  };

  Directive.$inject = [ 'ProcessDefinitionResource' ];

  module
    .directive('processDiagram', Directive);

});
