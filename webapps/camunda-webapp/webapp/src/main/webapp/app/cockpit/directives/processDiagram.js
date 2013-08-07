'use strict';

ngDefine('cockpit.directives', [
    'angular',
    'jquery',
    'bpmn/Bpmn',
    'jquery-overscroll',
    'jquery-mousewheel'
  ], function(module, angular, $, Bpmn) {

  function DirectiveController($scope, $element, $attrs, $filter, $location, $q, $window) {
    
    var w = angular.element($window);

    var bpmnElements;
    
    var activityHighligtClass = 'activity-highlight';
    var bpmnRenderer = null;
    var zoomLevel = 1;

    /*var selectedBpmnElementIds;*/
    
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
        bpmnElements = getBpmnElements();
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
    
    $scope.$on('resize', function () {
      $scope.$apply();
    });

    /*------------------- Handle annotations/incidents ---------------------*/

    var annotationsDone = false;
    var incidentsDone = false;

    $scope.$watch(function () { return bpmnRenderer; }, function (newValue) {
      if (newValue) {
        if (!annotationsDone && $scope.annotations) {
          annotations();
          annotationsDone = true;
        }

        if (!incidentsDone && $scope.incidents) {
          incidents();
          incidentsDone = true;
        }

      }
    });
    
    $scope.$watch('annotations', function(newValue) {
      if (newValue && bpmnRenderer && !annotationsDone) {
        annotations();
        annotationsDone = true;
      }
    });

    function annotations() {
      for (var i = 0, annotation; !!(annotation = $scope.annotations[i]); i++) {
        doAnnotate(annotation.id, annotation.count);
      }
    }
    
    $scope.$watch('incidents', function(newValue) {
      if (newValue && bpmnRenderer && !incidentsDone) {
        incidents();
        incidentsDone = true;
      }
    });
    
    function incidents() {
      for (var i = 0, activity; !!(activity = $scope.incidents[i]); i++) {
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
              if ($scope.selection && $scope.selection.view) {
                
                // get selected target
                var targetId = $($event.target).attr('id');
                
                if ($scope.clickableElements.indexOf(targetId) == -1) {
                  // in that case clear the selected bpmnElements

                  $scope.selection.view = {bpmnElements: []};
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
      var ctrlKey = $event.ctrlKey;

      if ($event.ctrlKey) {
        // if the 'ctrl' key has been pushed down, then select or deselect the clicked element
        
        if ($scope.selection.view && $scope.selection.view.bpmnElements) {
          var elements = [];

          var index = $scope.selection.view.bpmnElements.indexOf(selectedBpmnElement);
          
          var remove = false;

          if (index != -1) {
            // if the clicked element is already selected then deselect it.
            angular.forEach($scope.selection.view.bpmnElements, function (element) {
              if (element.id != selectedBpmnElement.id) {
                elements.push(element);
              }
            });

            remove = true;

          } else if (index == -1) {
            // if the clicked element is not already selected then select it together with other elements.
            elements.push(selectedBpmnElement);
            angular.forEach($scope.selection.view.bpmnElements, function (element) {
              elements.push(element);
            });
          }

          var view = $scope.selection.view = angular.extend({}, $scope.selection.view);
          
          // set the selected bpmn elements
          view['bpmnElements'] = elements;
          view['selectedBpmnElement'] = {element: selectedBpmnElement, ctrlKey: true, remove: remove};
          view['initialize'] = null;

          $scope.$apply();
          return;
        }
      }
      
      $scope.selection['view'] = {};
      $scope.selection.view['bpmnElements'] = [ selectedBpmnElement ];
      $scope.selection.view['selectedBpmnElement'] = {element: selectedBpmnElement, ctrlKey: false, remove: false};

      $scope.$apply();
    };

    /*------------------- Handle search parameter in location ---------------------*/

    $scope.$watch(function() { return $location.search().bpmnElements; }, function(newValue, oldValue) {
      function waitForBpmnElements() {
        var deferred = $q.defer();

        $scope.$watch(function () { return bpmnElements; }, function (newValue, oldValue) {
          if (!oldValue && newValue) {
            deferred.resolve(newValue);
          }
        })

        return deferred.promise;
      }

      if (!newValue && oldValue) {
        var view = $scope.selection.view = angular.extend({}, $scope.selection.view);
        view.bpmnElements = null;
        return;
      }

      if (newValue && angular.isArray(newValue) && !$scope.selection.view) {
        var searchParameter = newValue;

        if (bpmnElements) {
          selectBpmnElements(bpmnElements, searchParameter);
        } else {
          waitForBpmnElements().then(function (result) {
            selectBpmnElements(bpmnElements, searchParameter);
          });
        }
      }

      if (newValue && angular.isString(newValue)) {

        var searchParameter = newValue.split(',');

        if (searchParameter.length === 0) {
          $scope.selection.view = {bpmnElements: []};
          return;          
        }

        if (bpmnElements) {
          selectBpmnElements(bpmnElements, searchParameter);
        } else {
          waitForBpmnElements().then(function (result) {
            selectBpmnElements(bpmnElements, searchParameter);
          });
        }

      }
    });

    function selectBpmnElements (bpmnElements, searchParameter) {
      var selectedBpmnElements = [];
      for(var i = 0, selection; !!(selection = searchParameter[i]); i++) {
        var bpmnElement = bpmnElements[selection];
        if (bpmnElement) {
          selectedBpmnElements.push(bpmnElement);
        }
      }
      $scope.selection['view'] = {};
      $scope.selection.view['initialize'] = {bpmnElements: selectedBpmnElements}
      $scope.selection.view['scrollToBpmnElement'] = selectedBpmnElements[selectedBpmnElements.length-1];
      $scope.selection.view['bpmnElements'] = selectedBpmnElements;
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
      if ($scope.selection) {
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
    }
    
    /*------------------- Handle selected activity id---------------------*/
    
    $scope.$watch('selection.view.bpmnElements', function(newValue, oldValue) {
      var selectedBpmnElementIds = [];
      var searchParameter = $location.search().bpmnElements;

      if (searchParameter && angular.isString(searchParameter)) {
        selectedBpmnElementIds = searchParameter.split(',');
      } else if (searchParameter && angular.isArray(searchParameter)) {
        selectedBpmnElementIds = angular.copy(searchParameter);
      }

      if (oldValue) {
        angular.forEach(oldValue, function(bpmnElement) {
          deselectActivity(bpmnElement);
          if (selectedBpmnElementIds) {
            var index = selectedBpmnElementIds.indexOf(bpmnElement.id);
            if (index !== -1) {
              selectedBpmnElementIds.splice(index, 1);
            }
          }
        });
      }
      if (newValue) {
        angular.forEach(newValue, function(bpmnElement) {
          selectActivity(bpmnElement);
          var index = selectedBpmnElementIds.indexOf(bpmnElement.id);
          if (index === -1) {
            selectedBpmnElementIds.push(bpmnElement.id);
          }
        });
      }
      if (selectedBpmnElementIds.length === 0) {
        $location.search('bpmnElements', null);
      } else {
        $location.search('bpmnElements', selectedBpmnElementIds);
      }
      $location.replace();
    });

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
    
    $scope.$watch('selection.view.scrollToBpmnElement', function(newValue) {
      if (newValue) {
        scrollTo(newValue)
      }
    });

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

    function getBpmnElements () {
      var result = {};

      var key = $scope.processDefinition.key;

      var diagram = null;

      for (var i = 0; i < $scope.processDiagram.length; i++) {
        var currentDiagram = $scope.processDiagram[i];
        if (currentDiagram.type === 'process') {
          
          if (currentDiagram.id === key) {
            diagram = currentDiagram;
            break;
          }
        }
      }

      getBpmnElementsHelper(diagram, result);

      return result;

    }

    function getBpmnElementsHelper(element, result) {
      result[element.id] = element;

      if (element.baseElements) {
        angular.forEach(element.baseElements, function(baseElement) {
          getBpmnElementsHelper(baseElement, result);
        });
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
