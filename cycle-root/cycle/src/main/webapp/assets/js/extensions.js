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

(function(angular) {
  
  /**
   * Implementing the $urlProvider
   */
  function $UrlProvider() {

    function UrlBase(document) {
      var self = this, 
          baseElement = document.find("base"), 
          pathSeparator = null;

      /**
       * Returns current <base href>
       * (always relative - without domain)
       *
       * @returns {string=}
       */
      function relativeUri(uri) {
        return uri ? uri.replace(/^https?\:\/\/[^\/]*/, '') : uri;
      }

      self.pathSeparator = function() {
        if (pathSeparator) {
          return pathSeparator;
        }

        var current = relativeUri(baseElement.attr("href")), 
            base = relativeUri(baseElement.attr("app-base"));

        var localUri = current.replace(base, "");
        var pathElements = localUri.split("/");

        var relativeBase = "";
        for (var i = 0; i < pathElements.length - 1; i++) {
          relativeBase += "../";
        }

        pathSeparator = relativeBase;
        return pathSeparator;
      };

      /**
       * Converting a project local uri into an uri which is 
       * independant of the current location in the application
       */
      self.absolute = function(uri) {
        return self.pathSeparator() + uri;
      };
    }
    
    this.$get = ['$document', function($document) {
      return new UrlBase($document);
    }];
  }
  
  angular.module('ng.extensions', [], ['$provide',
    function ngExtensionModule($provide) {
      $provide.provider('$url', $UrlProvider);
    }]);
})(angular);