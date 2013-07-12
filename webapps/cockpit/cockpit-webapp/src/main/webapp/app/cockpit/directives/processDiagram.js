'use strict';

ngDefine('cockpit.directives', [
    'angular',
    'jquery',
    'bpmn/Bpmn',
    'jquery-overscroll',
    'jquery-mousewheel'
  ], function(module, angular, $, Bpmn) {

  function DirectiveController($scope, $element, $attrs, $filter, $window) {
    
    var w = angular.element($window);
    
    var activityHighligtClass = 'activity-highlight';
    var bpmnRenderer = null;
    var zoomLevel = 1;
    
    $scope.$on('$destroy', function() {
      bpmnRenderer = null;
      $scope.processDiagram = null;
    });
    
    /*------------------- Rendering of process diagram ---------------------*/

    /**
     * If the process diagram changes, then the diagram will be rendered.
     */
    $scope.$watch('processDiagram', function(newValue, oldValue) {
      if (newValue) {
        bpmnRenderer = new Bpmn();
        renderDiagram();
        initializeScrollAndZoomFunctions();
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
      $element.addClass('process-diagram');
      var options = {
        diagramElement : $element.attr('id')
      };

      // do the rendering
      bpmnRenderer.renderDiagram($scope.processDiagram, options);
    }
    
    /*------------------- Handle scroll and zoom ---------------------*/
    
    
    $scope.$watch(function() { return zoomLevel; }, function(newZoomLevel) {
      if (!!newZoomLevel && !!bpmnRenderer) {
        zoom(newZoomLevel);
      }
    });
    
    function initializeScrollAndZoomFunctions() {
      zoom(zoomLevel);
      
      $element.mousewheel(function($event, delta) {
        $event.preventDefault();
        $scope.$apply(function() {
          zoomLevel = calculateZoomLevel(delta);
        });
      });
    }
    
    function overscroll() {
      $element.overscroll({captureWheel:false});
    }

    function removeOverscroll() {
      $element.removeOverscroll();
    }

    function zoom(zoomFactor) {
      removeOverscroll();
      bpmnRenderer.zoom(zoomFactor);
      overscroll();
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
    
    /*------------------- Handle window resize ---------------------*/
    
    w.bind('resize', function () {
      $scope.$apply();
    });
    
    $scope.$watch(function () {
      return $element.width();
    }, function(newValue, oldValue) {
      if (bpmnRenderer) {
        zoom(zoomLevel);
      }
    });
    
    $scope.$watch(function () {
      return $element.height();
    }, function(newValue, oldValue) {
      if (bpmnRenderer) {
        zoom(zoomLevel);
      }
    });
    
    /*------------------- Handle annotations/incidents ---------------------*/
    
    $scope.$watch('annotations', function(newValue) {
      if (newValue) {
        annotations();
      }
    });

    function annotations() {
      angular.forEach($scope.annotations, function (annotation) {
        doAnnotate(annotation.id, annotation.count);
      });
    }
    
    $scope.$watch('incidents', function(newValue) {
      if (newValue) {
        incidents();
      }
    });
    
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
      // get the div element containing the badges (i.e. annotations/incidents).
      var badge = $('#' + $element.attr('id') + ' > #' + activityId + ' > .badge-position');
      if (badge.length > 0) {
        // get the incident badge
        var importantBadge = $('#' + $element.attr('id') + ' > #' + activityId + ' > .badge-position > .badge-important');
        
        if (importantBadge.length > 0) {
          // if an element with class '.badge-important' (incident badge) already exists, then prepend the innerHtml
          badge.prepend(innerHtml);
        } else {
          // if an element with class '.badge-important' (incident badge) does not exist, then append the innerHtml
          badge.append(innerHtml);
        }
      } else {
        // in that case there no badges are existings, so the first one will initially added.
        try {
          bpmnRenderer.annotation(activityId).addDiv(innerHtml, ['badge-position']);
        } catch (error) {
          console.log('Could not annotate activity \'' + activityId + '\': ' + error.message);
        }
      }
    }    
    
    /*------------------- Register click events ---------------------*/
    
    $scope.$watch('clickableElements', function(newValue) {
      if (newValue) {
        registerEventHandlersOnProcessDiagram();
        registerEventHandlersOnBaseElements();
      }
    });  
    
    function registerEventHandlersOnProcessDiagram() {
      
      var moved = false;
      var mousedown = false;
      // register event handler on $element: mousedown, mousemove, mouseup
      $element
        // register mousedown event
        .mousedown(function($event) {
          mousedown = true;
        })
        // register mousemove event
        .mousemove(function($event) {
          if (mousedown) {
            // set 'moved' true, when there was
            // a mousedown event at first.
            moved = true;
          }
        })
        // register mouseup event
        .mouseup(function($event) {
          if (!$event.ctrlKey) {
            
            if (!moved && mousedown) {
              // if the mouse have not moved and a mousedown happend,
              // then you have to deselect the current selection.
              if ($scope.selection && $scope.selection.treeDiagramMapping) {
                
                // get selected target
                var targetId = $($event.target).attr('id');
                
                if ($scope.clickableElements.indexOf(targetId) == -1) {
                  // in that case clear the selected bpmnElements
                  $scope.selection.treeDiagramMapping = {bpmnElements: []};
                  $scope.$apply();
                }
              }              
            }
          }
          // always reset the values
          moved = false;
          mousedown = false;
        });
    }
    
    /* Handler to handle click event on a clickable bpmn element */
    var clickHandler = function($event) {
      var selectedBpmnElement = $event.data;
      
      if ($event.ctrlKey) {
        // if the 'ctrl' key has been pushed down, then select or deselect the clicked element
        
        if ($scope.selection.treeDiagramMapping && $scope.selection.treeDiagramMapping.bpmnElements) {
          var elements = [];
          
          var index = $scope.selection.treeDiagramMapping.bpmnElements.indexOf(selectedBpmnElement);
          
          if (index != -1) {
            // if the clicked element is already selected then deselect it.
            angular.forEach($scope.selection.treeDiagramMapping.bpmnElements, function (element) {
              if (element.id != selectedBpmnElement.id) {
                elements.push(element);
              }
            });
            
          } else if (index == -1) {
            // if the clicked element is not already selected then select it together with other elements.
            elements.push(selectedBpmnElement);
            angular.forEach($scope.selection.treeDiagramMapping.bpmnElements, function (element) {
              elements.push(element);
            });            
          }
          
          // set the selected bpmn elements
          $scope.selection.treeDiagramMapping = {bpmnElements: elements};
          $scope.$apply();
          return;
        }
      }
      
      
      $scope.selection.treeDiagramMapping = {bpmnElements: [ selectedBpmnElement ]};
      $scope.$apply();
    };
    
    function registerEventHandlersOnBaseElements() {
      if ($scope.selection) {
        var model = selectProcessObject(); 
        registerEventHandlersOnBaseElement(model);
      }
    }
    
    function registerEventHandlersOnBaseElement(element) {

      if ($scope.clickableElements.indexOf(element.id) != -1) {
        $('#' + element.id)
        
          // register click
          .click(element, clickHandler)
          
          // mouseover
          .mouseover(element, function($event) {
            if (!element.isSelected && $scope.clickableElements.indexOf($event.data.id) != -1) {
              // add css class to highlight activity
              bpmnRenderer.annotation($event.data.id).addClasses([ activityHighligtClass ]);
              // add pointer ass cursor
              $('#' + element.id).css('cursor', 'pointer');
            }
          })
          
          // mouseout
          .mouseout(element, function($event){
            if (!element.isSelected && $scope.clickableElements.indexOf($event.data.id) != -1) {
              // remove css class to highlight activity
              bpmnRenderer.annotation($event.data.id).removeClasses([ activityHighligtClass ]);
            }          
          });
      }
      
      if (element.baseElements) {
        angular.forEach(element.baseElements, function(baseElement) {
          registerEventHandlersOnBaseElement(baseElement);
        });
      }
    }
    
    /*------------------- Handle selected activity id---------------------*/
    
    $scope.$watch('selection.treeDiagramMapping.bpmnElements', function(newValue, oldValue) {
      if (oldValue) {
        deselectActivity(oldValue);
      }
      if (newValue) {
        selectActivity(newValue);
      }
    });

    function selectActivity(bpmnElements) {
      angular.forEach(bpmnElements, function(bpmnElement) {
        if (bpmnElement) {
          bpmnElement.isSelected = true;
          try {
            bpmnRenderer.annotation(bpmnElement.id).addClasses([ activityHighligtClass ]);
          } catch (error) {
            console.log('Could not add css class ' + activityHighligtClass + ' on element ' + bpmnElement.id + ': ' + error.message);
          }
        }
      });
    }
    
    function deselectActivity(bpmnElements) {
      angular.forEach(bpmnElements, function(bpmnElement) {
        if (bpmnElement) {
          bpmnElement.isSelected = false;
          try {
            bpmnRenderer.annotation(bpmnElement.id).removeClasses([ activityHighligtClass ]);
          } catch (error) {
            console.log('Could not remove css class ' + activityHighligtClass + ' on element ' + bpmnElement.id + ': ' + error.message);
          }
        }
      });
    }
    
    $scope.$watch('selection.treeDiagramMapping.scrollToBpmnElement', function(newValue) {
      if (newValue) {
        
        // parent size
        var parentElementHeight = $element.height();
        var parentElementWidth = $element.width();
        
        // get the bpmn element to scroll to
        var bpmnElement = $('#' + $element.attr('id') + '> #' + newValue.id);

        // get the height and width of the bpmn element
        var bpmnElementHeight = bpmnElement.height();
        var bpmnElementWidth = bpmnElement.width();
        
        // get the top and left position of the bpmn element
        var bpmnElementTop = parseInt(bpmnElement.css('top'));
        var bpmnElementLeft = parseInt(bpmnElement.css('left'));
        
        var scrollTop = (bpmnElementTop +  (bpmnElementHeight/2)) - parentElementHeight/2;
        var scrollLeft = (bpmnElementLeft +  (bpmnElementWidth/2)) - parentElementWidth/2;
        
        $element.animate({
          scrollTop: scrollTop,
          scrollLeft: scrollLeft
        });
      }
    });
    
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
    
    this.getRenderer = function () {
      return bpmnRenderer;
    };
    
  }

  var Directive = function ($window) {
    return {
      restrict: 'EAC',
      scope: {
        processDiagram: '=',
        processDefinition: '=',
        selection: '=',
        annotations: '=',
        incidents: '=',
        clickableElements: '='
      },
      controller: DirectiveController
    };
  };
  
  Directive.$inject = [ '$window' ];

  module
    .directive('processDiagram', Directive);

});
