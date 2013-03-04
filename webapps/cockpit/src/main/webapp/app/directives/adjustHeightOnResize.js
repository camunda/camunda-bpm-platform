"use strict";

define([ "angular", "jquery"], function(angular, $) {
  
  var module = angular.module("cockpit.directives");
  
  var Directive = function () {
    return {
      restrict: 'A',
      link: function(scope, element, attrs, $destroy) {
        
        scope.getWindowHeight = function() {
          return $(window).height();
        };
        
        window.onresize = function () {
          scope.$apply();
        };
        
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
        
        $(element).css("height", (scope.getWindowHeight() - 82 - 31) + "px");
      }
    };
  };
  
  
  module
    .directive('adjustHeightOnResize', Directive);
  
});