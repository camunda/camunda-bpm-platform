'use strict';

ngDefine('cockpit.directives', [
    'angular',
    'jquery',
    'bpmn/Bpmn',
    'jquery-overscroll',
    'jquery-mousewheel'
  ], function(module, angular, $, Bpmn) {

  function DirectiveController($scope, $element, $attrs, $filter, $q, $window) {
    
    var w = angular.element($window);

    var bpmnElements,
        selection,
        scrollToBpmnElementId;
    
    var activityHighligtClass = 'activity-highlight';
    var bpmnRenderer = null;
    var zoomLevel = 1;
   
    $scope.$on('$destroy', function() {
      bpmnRenderer = null;
      $scope.processDiagram = null;
      $scope.processDiagramOverlay = null;
    });
    
    /*------------------- Rendering of process diagram ---------------------*/

    /**
     * If the process diagram changes, then the diagram will be rendered.
     */
    $scope.$watch('processDiagram', function(newValue, oldValue) {
      if (newValue && newValue.$loaded !== false) {
        try {
          bpmnElements = newValue.bpmnElements;
          bpmnRenderer = new Bpmn();
          renderDiagram();
          initializeScrollAndZoomFunctions();

          // update selection in case it has been provided earlier
          updateSelection(selection);

          // update scroll to in case it has been provided earlier
          scrollToBpmnElement(scrollToBpmnElementId);
        } catch (exception) {
          console.log('Unable to render diagram for process definition ' + $scope.processDiagram.processDefinition.id + ', reason: ' + exception.message)
          element.html('<p style="text-align: center;margin-top: 100px;">Unable to render process diagram.</p>')
        }
      }
    });
    
    $scope.$watch('processDiagramOverlay', function(newValue, oldValue) {
      if (newValue) {
        if (newValue.annotations) {
          annotations();  
        }

        if (newValue.incidents) {
          incidents();  
        }        

        if (newValue.clickableElements) {
          registerEventHandlersOnProcessDiagram();
          registerEventHandlersOnBaseElements(); 
        }
      }
    });

    function renderDiagram() {
      
      // set the element id to processDiagram_*
      var elementId = 'processDiagram_' + $scope.processDiagram.processDefinition.id.replace(/:/g, '_');
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
      bpmnRenderer.renderDiagram($scope.processDiagram.semantic, options);
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
    
    $scope.$on('resize', function () {
      $scope.$apply();
    });

    /*------------------- Handle annotations/incidents ---------------------*/

    function annotations() {
      for (var i = 0, annotation; !!(annotation = $scope.processDiagramOverlay.annotations[i]); i++) {
        doAnnotate(annotation.id, annotation.count);
      }
    }
    
    function incidents() {
      for (var i = 0, activity; !!(activity = $scope.processDiagramOverlay.incidents[i]); i++) {
        if (activity.incidents && activity.incidents.length > 0) {
          executeAnnotation(activity.id, '<p class="badge badge-important">!</p>');
        }
      }
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
          var targetId = $($event.target).attr('id'),
              idx = $scope.processDiagramOverlay.clickableElements.indexOf(targetId),
              ctrlKey = $event.ctrlKey;

          if (!ctrlKey) {
            
            if (!moved && mousedown) {
              // if the mouse have not moved and a mousedown happend,
              // then you have to deselect the current selection.
              if ($scope.onElementClick && idx === -1) {
                $scope.onElementClick({id: null, $event: $event});
                $scope.$apply();
              }
            }
          }
          // always reset the values
          moved = false;
          mousedown = false;
        });
    }

    function registerEventHandlersOnBaseElements() {
      function waitForBpmnElements () {
        var deferred = $q.defer();
        $scope.$watch(function () { return bpmnElements; }, function (newValue) {
          if (newValue) {
            deferred.resolve(newValue);
          }
        });
        return deferred.promise;
      }
      if ($scope.onElementClick) {
        if (bpmnElements) {
          for (var key in bpmnElements) {
            registerEventHandlersOnBaseElement(bpmnElements[key]);
          }          
        } else {
          waitForBpmnElements().then(function(bpmnElements) {
            for (var key in bpmnElements) {
              registerEventHandlersOnBaseElement(bpmnElements[key]);
            }   
          });
        }
      }
    }
    
    function registerEventHandlersOnBaseElement(element) {
      var elementId = element.id;
      if ($scope.processDiagramOverlay.clickableElements.indexOf(elementId) != -1) {
        $('#' + elementId)
        
          // register click
          .click(elementId, function ($event) {
            $scope.onElementClick({id: $event.data, $event: $event});
            $scope.$apply();
          })
          
          // mouseover
          .mouseover(elementId, function($event) {
            if (!element.isSelected && $scope.processDiagramOverlay.clickableElements.indexOf($event.data) !== -1) {
              // add css class to highlight activity
              bpmnRenderer.annotation($event.data).addClasses([ activityHighligtClass ]);
              // add pointer ass cursor
              $('#' + element.id).css('cursor', 'pointer');
            }
          })
          
          // mouseout
          .mouseout(elementId, function($event){
            if (!element.isSelected && $scope.processDiagramOverlay.clickableElements.indexOf($event.data) !== -1) {
              // remove css class to highlight activity
              bpmnRenderer.annotation($event.data).removeClasses([ activityHighligtClass ]);
            }          
          });
      }
    }
    
    /*------------------- Handle selected activity id---------------------*/
    
    $scope.$watch('selection.activityIds', function(newValue, oldValue) {
      updateSelection(newValue);
    });

    function updateSelection(newSelection) {
      if (bpmnElements) {
        if (selection) {
          angular.forEach(selection, function(elementId) {
            var bpmnElement = bpmnElements[elementId];
            deselectActivity(bpmnElement);
          });
        }

        if (newSelection) {
          angular.forEach(newSelection, function(elementId) {
            var bpmnElement = bpmnElements[elementId];
            selectActivity(bpmnElement);
          });
        }
      }

      selection = newSelection;
    }

    function selectActivity(bpmnElement) {
      if (bpmnElement) {
        bpmnElement.isSelected = true;
        try {
          bpmnRenderer.annotation(bpmnElement.id).addClasses([ activityHighligtClass ]);
        } catch (error) {
          console.log('Could not add css class ' + activityHighligtClass + ' on element ' + bpmnElement.id + ': ' + error.message);
        }
      }
    }
    
    function deselectActivity(bpmnElement) {
      if (bpmnElement) {
        bpmnElement.isSelected = false;
        try {
          bpmnRenderer.annotation(bpmnElement.id).removeClasses([ activityHighligtClass ]);
        } catch (error) {
          console.log('Could not remove css class ' + activityHighligtClass + ' on element ' + bpmnElement.id + ': ' + error.message);
        }
      }
    }

    /*------------------- Handle scroll to bpmn element ---------------------*/
    
    $scope.$watch('selection.scrollToBpmnElement', function(newValue) {
      if (newValue) {
        scrollToBpmnElement(newValue);
      }
    });

    function scrollToBpmnElement(bpmnElementId) {
      if (bpmnElements) {
        var bpmnElement = bpmnElements[bpmnElementId];
        if (bpmnElement) {
          scrollTo(bpmnElement)  
        }
      }
      scrollToBpmnElementId = bpmnElementId;
    }    

    function scrollTo(element) {
      // parent size
      var parentElementHeight = $element.height();
      var parentElementWidth = $element.width();
      
      // get the bpmn element to scroll to
      var bpmnElement = $('#' + $element.attr('id') + '> #' + element.id);

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

    this.getRenderer = function () {
      return bpmnRenderer;
    };
    
  }

  var Directive = function ($window) {
    return {
      restrict: 'EAC',
      scope: {
        processDiagram: '=',
        processDiagramOverlay: '=',
        onElementClick: '&',
        selection: '='
      },
      controller: DirectiveController
    };
  };
  
  Directive.$inject = [ '$window' ];

  module
    .directive('processDiagram', Directive);

});
