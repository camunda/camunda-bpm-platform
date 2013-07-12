'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var Directive = function () {
    return {
      restrict: 'EAC',
      link: function(scope, element, attrs) {
        
        var id = element.attr('id');
        
        scope.$watch(attrs['resize'], function (newValue) {
          if (!newValue) {
            return;
          }
          
          if (newValue.toShrink) {
            if (id) {
              angular.forEach(newValue.toShrink, function(elementId) {
                if (id === elementId) {
                  element.css('width', '');
                  element.css('height', '');
                }
              });
            }            
            return;
          }
          
          if (newValue.toGreater) {
            if (id) {
              angular.forEach(newValue.toGreater, function(elementId) {
                if (id === elementId) {
                  element.css('width', '100%');
                  element.css('height', '100%');
                }
              });
            }            
            return;
          }

        });
        
      }
    };
  };
  
  module
    .directive('resize', Directive);
  
});
