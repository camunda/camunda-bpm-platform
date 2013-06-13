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
  
  function DirectiveController($scope, $element, $attrs, $filter, ProcessDiagramService) {

    var activityStatistics = null;
    var activityInstances = null;
    
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
      ProcessDiagramService.getBpmn20Xml(processDefinitionId)
      .then(
          function(data) {
            if (miniature && miniature === true) {
              renderMiniatureProcessDiagram(data.bpmn20Xml);
            } else {
              renderProcessDiagram(data.bpmn20Xml);
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
        } else if (activityInstances) {
          doAnnotateWithActivityInstances(activityInstances);
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
      
      var children = instance.childInstances;
      
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
    }
    
    function doAnnotate(activityId, count) {
      var shortenNumberFilter = $filter('shortenNumber');
      bpmnRenderer.annotate(activityId, '<p class="badge badgePosition">' + shortenNumberFilter(count) + '</p>');
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
    
  }
  
  var Directive = function (ProcessDiagramService) {
    return {
      restrict: 'EAC',
      controller: DirectiveController 
    };
  };

  Directive.$inject = [ 'ProcessDiagramService' ];
  
  module
    .directive('processDiagram', Directive);
  
});
