'use strict';

/* Extension Directives */

(function (angular) {  
  
  var ng = angular.module('ng');
  
  /*
   * Defines the ng:if tag. This is useful if jquery mobile does not allow
   * an ng-switch element in the dom, e.g. between ul and li.
   */
  var ngIfDirective = {
    transclude:'element',
    priority:1000,
    terminal:true,
    restrict: 'A',
    compile:function (element, attr, linker) {
      return function (scope, iterStartElement, attr) {
        iterStartElement[0].doNotMove = true;
        var expression = attr.ngmIf;
        var lastElement;
        var lastScope;
        scope.$watch(expression, function (newValue) {
          if (lastElement) {
            lastElement.remove();
            lastElement = null;
          }
          lastScope && lastScope.$destroy();
          if (newValue) {
            lastScope = scope.$new();
            linker(lastScope, function (clone) {
              lastElement = clone;
              iterStartElement.after(clone);
            });
          }
          // Note: need to be parent() as jquery cannot trigger events on comments
          // (angular creates a comment node when using transclusion, as ng-repeat does).
          iterStartElement.parent().trigger("$childrenChanged");
        });
      };
    }
  };
  
  ng.directive('ngmIf', function () {
    return ngIfDirective;
  });
})(angular);