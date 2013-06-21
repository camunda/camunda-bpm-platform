'use strict';

ngDefine('cockpit.directives', [
    'angular',
    'jquery',
    'bpmn/Bpmn'
  ], function(module, angular, $, Bpmn) {

  function DirectiveController($scope, $element, $attrs, $filter) {

    /*------------------- Rendering of process diagram ---------------------*/
    
    var bpmnRenderer = null;
    
    var miniature = $scope.$eval($attrs['miniature']);

    $scope.$on('$destroy', function() {
      bpmnRenderer = null;
      $scope.processDiagram = null;
    });

    /**
     * If the process diagram changes, then the diagram will be rendered.
     */
    $scope.$watch('processDiagram', function(newValue, oldValue) {
      if (newValue) {
        bpmnRenderer = new Bpmn();
        renderDiagram();
        registerClickEventOnBaseElements();
        annotations();
        incidents();
      }
    });
    
    function renderDiagram() {
      
      // set the element id to processDiagram_*
      var elementId = 'processDiagram_' + $scope.processDefinition.id.replace(/:/g, '_');
      $element.attr('id', elementId);

      // clear innerHTML of element in case that the process diagram has changed 
      // and the old one has been rendered.
      $element.empty();
      
      // set the render options
      var options = null;
      if (miniature && miniature === true) {
        options = {
            diagramElement : $element.attr('id'),
            width : parseInt($element.parent().css("min-width")),
            height : $element.parent().height(),
          };
      } else {
        $element.addClass('process-diagram');
        options = {
            diagramElement : $element.attr('id')
        };
      }

      // do the rendering
//      bpmnRenderer = new Bpmn();
      bpmnRenderer.renderDiagram($scope.processDiagram, options);
    }
    
    /*------------------- Register click events ---------------------*/
    
    function registerClickEventOnBaseElements() {
      if ($scope.selection) {
        var model = selectProcessObject(); 
        registerClickEvent(model);
      }
    }
    
    function registerClickEvent(element) {

//    bpmnRenderer.on('click', element, function($event, baseElement) {
//      if ($scope.selection) {
//        $scope.selection.activityId = baseElement.id;
//      }
//    });
      
      $('#' + element.id).on('click', element, function($event) {
        $scope.selection.selectedActivityIdsInProcessDiagram = [];
        $scope.selection.selectedBpmnElements = [];
        $scope.selection.selectedBpmnElements.push($event.data);
        $scope.$apply();
      });
      
      if (element.baseElements) {
        angular.forEach(element.baseElements, function(baseElement) {
          registerClickEvent(baseElement);
        });
        
      }
    }
    
    /*------------------- Handle selected activity id---------------------*/
    
    var activityHighligtClass = 'activity-highlight';
    
    $scope.$watch(function () {
      if ($scope.selection) {
        return $scope.selection.selectedBpmnElements;
      }
      return null;
    }, function(newValue, oldValue) {
      if (newValue) {
        selectActivity(newValue);
      }
      if (oldValue) {
        deselectActivity(oldValue);
      }
    });
    
    function selectActivity(bpmnElements) {
      angular.forEach(bpmnElements, function(bpmnElement) {
        bpmnRenderer.annotation(bpmnElement.id).addClasses([ activityHighligtClass ]);
      });
    }
    
    function deselectActivity(bpmnElements) {
      angular.forEach(bpmnElements, function(bpmnElement) {
        bpmnRenderer.annotation(bpmnElement.id).removeClasses([ activityHighligtClass ]);
      });
    }

    $scope.$watch(function () {
      if ($scope.selection) {
        return $scope.selection.selectedActivityIdsInProcessDiagram;
      }
      return null;
    }, function(newValue, oldValue) {
      if (newValue) {
        selectActivitiesWithIds(newValue);
      }
      if (oldValue) {
        deselectActivitiesWithIds(oldValue);
      }
    });
    
    function selectActivitiesWithIds(activityIds) {
      angular.forEach(activityIds, function(activityId) {
        bpmnRenderer.annotation(activityId).addClasses([ activityHighligtClass ]);
      });
    }
    
    function deselectActivitiesWithIds(activityIds) {
      angular.forEach(activityIds, function(activityId) {
        bpmnRenderer.annotation(activityId).removeClasses([ activityHighligtClass ]);
      });
    }
    
    /*------------------- Handle annotation/incidents ---------------------*/
    
    function annotations() {
      angular.forEach($scope.annotations, function (annotation) {
        doAnnotate(annotation.id, annotation.count);
      });
    }
    
    function incidents() {
      angular.forEach($scope.incidents, function (activity) {
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
      var badge = $('#' + $element.attr('id') + ' > #' + activityId + ' > .badgePosition');
      if (badge.length > 0) {
        var importantBadge = $('#' + $element.attr('id') + ' > #' + activityId + ' > .badgePosition > .badge-important');
        if (importantBadge.length > 0) {
          badge.prepend(innerHtml);
        } else {
          badge.append(innerHtml);
        }
      } else {
        bpmnRenderer.annotation(activityId).addDiv(innerHtml, ['badgePosition']);
      }
    }
    
    /**
     * Iterate over processDiagram and check whether the
     * process definition key is equals to process diagram id
     * and whether the process diagram is a process.
     * If the conditions match then the corresponding process
     * diagram will be returned.
     */
    function selectProcessObject() {
      var key = $scope.processDefinition.key;
      
      for (var i = 0; i < $scope.processDiagram.length; i++) {
        var currentDiagram = $scope.processDiagram[i];
        if (currentDiagram.type === 'process') {
          
          if (currentDiagram.id === key) {
            return currentDiagram;
          }
        }
      }
    }
    

    
//    function annotate() {
//      if (bpmnRenderer) {
//
//        if (activityStatistics) {
//          doAnnotateWithActivityStatistics(activityStatistics);
//          // set to null, so that the number will not set twice
//          activityStatistics = null;
//        } else if (activityInstances) {
//          doAnnotateWithActivityInstances(activityInstances);
//          // set to null, so that the number will not set twice
//          activityInstances = null;
//        }
//
//        if (activitiesWithIncidents) {
//          doAnnotateWithIncidents(activitiesWithIncidents);
//          // set to null, so that the incidents will not set twice
//          activitiesWithIncidents = null;
//        }
//      }
//    }
//
//    function doAnnotateWithActivityStatistics(activityStaticstics) {
//      angular.forEach(activityStatistics, function (currentActivityStatistics) {
//        doAnnotate(currentActivityStatistics.id, currentActivityStatistics.instances);
//      });
//    }
//
//    function doAnnotateWithActivityInstances(activityInstances) {
//      for (var key in activityInstances) {
//        var mappings = activityInstances[key];
//        doAnnotate(key, mappings.length);
//      }
//    }
//
//    function doAnnotateWithIncidents(activitiesWithIncidents) {
//      angular.forEach(activitiesWithIncidents, function (activity) {
//        if (activity.incidents && activity.incidents.length > 0) {
//          executeAnnotation(activity.id, '<p class="badge badge-important">!</p>');
//        }
//      });
//    }
//
//    function doAnnotate(activityId, count) {
//      var shortenNumberFilter = $filter('shortenNumber');
//      executeAnnotation(activityId, '<p class="badge">' + shortenNumberFilter(count) + '</p>');
//    }
//
//    function executeAnnotation(activityId, innerHtml) {
//      var badge = $('#' + $element.attr('id') + ' > #' + activityId + ' > .badgePosition');
//      if (badge.length > 0) {
//        var importantBadge = $('#' + $element.attr('id') + ' > #' + activityId + ' > .badgePosition > .badge-important');
//        if (importantBadge.length > 0) {
//          badge.prepend(innerHtml);
//        } else {
//          badge.append(innerHtml);
//        }
//      } else {
//        bpmnRenderer.annotation(activityId).addDiv(innerHtml, ['badgePosition']);
//      }
//    }
//    
//    this.annotateWithActivityStatistics = function (statistics) {
//      activityStatistics = statistics;
//      annotate();
//    };
//
//    this.annotateWithActivityInstances = function (instances) {
//      activityInstances = instances;
//      annotate();
//    };
//    
//    this.annotateWithIncidents = function (activities) {
//      activitiesWithIncidents = activities;
//      annotate();
//    };
//    
    this.getRenderer = function () {
      return bpmnRenderer;
    };
    
  }

  var Directive = function () {
    return {
      restrict: 'EAC',
      scope: {
        processDiagram: '=',
        processDefinition: '=',
        selection: '=',
        annotations: '=',
        incidents: '=',
      },
      controller: DirectiveController
    };
  };

  module
    .directive('processDiagram', Directive);

});
