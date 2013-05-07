"use strict";

define([ "angular", "jquery"], function(angular, $) {
  
  var module = angular.module("cockpit.directives");
  
  var Directive = function ($window) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        
        var w = angular.element($window);
        
        scope.getWindowHeight = function() {
          return w.height();
        };
        
        w.bind('resize', function () {
          scope.$apply();
        });

        scope.$watch(function () {
          return scope.getWindowHeight();
        }, function(newValue, oldValue) {
          if (newValue === oldValue) {
            return;
          }
          var height = parseInt($(element).css("height"));
          var newHeight = height + newValue - oldValue;
          
          $(element).css("height", newHeight + "px");
        });
        
        var headerHeight = $('.navbar-fixed-top').outerHeight(false);
        var footerHeight = $('footer').outerHeight(false);
        var paddingTop = parseInt($(element).css("padding-top"));
        var paddingBottom = parseInt($(element).css("padding-bottom"));
        var borderTop = parseInt($(element).css("border-top-width"));
        var borderBottom = parseInt($(element).css("border-bottom-width"));
        
        $(element).css("height", (scope.getWindowHeight() - headerHeight - footerHeight - paddingTop - paddingBottom - borderTop - borderBottom) + "px");
      }
    };
  };
  
  module.$inject = ["$window"];
  
  
  module
    .directive('fillHeight', Directive);
  
});