'use strict';

ngDefine('cockpit.directives', [ 'angular',
                                 'bootstrap-slider',
                                 'jquery-overscroll',
                                 'jquery-mousewheel' 
                               ], function(module, angular) {
  
  var Directive = function () {
    return {
      restrict: 'AC',
      require: 'processDiagram', 
      link: function(scope, element, attrs, processDiagram) {
        
        var zoomLevel = 1;
        
        scope.$watch(function() { return zoomLevel; }, function(newZoomLevel) {
          if (!!newZoomLevel && !!processDiagram.getRenderer()) {
            zoom(newZoomLevel);
          }
        });
        
        scope.$watch(function() { return processDiagram.getRenderer(); }, function (newValue) {
          if (newValue) {
            zoom(zoomLevel);
            
            element.mousewheel(function(event, delta) {
              scope.$apply(function() {
                zoomLevel = calculateZoomLevel(delta);
              });
            });
          }
        });
        
        function overscroll() {
          element.overscroll({captureWheel:false});
        }

        function removeOverscroll() {
          element.removeOverscroll();
        }

        function zoom(zoomFactor) {
          removeOverscroll();
          processDiagram.getRenderer().zoom(zoomFactor);
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
        
      }
    };
  };
  
  module
    .directive('onScrollAndZoom', Directive);
  
});
