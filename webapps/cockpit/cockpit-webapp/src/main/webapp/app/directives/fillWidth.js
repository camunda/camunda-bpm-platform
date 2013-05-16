"use strict";

define([ "angular", "jquery"], function(angular, $) {
  
  var module = angular.module("cockpit.directives");
  
  var Directive = function ($window) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        
        var w = angular.element($window);
        
        scope.getWindowWidth = function() {
          return w.width();
        };
        
        w.bind('resize', function () {
          scope.$apply();
        });

        scope.$watch(function () {
          return scope.getWindowWidth();
        }, function(newValue, oldValue) {
          if (newValue === oldValue) {
            return;
          }
          var width = $(element).width();
          var newWidth = width + newValue - oldValue;
          
          $("#processDiagram").removeOverscroll();
          $(element).css("width", newWidth+ "px");
          $("#processDiagram").overscroll({captureWheel:false});
          
        });
        
        var leftPanelElement = $('.left-panel');
        var leftPanelWidth = leftPanelElement.outerWidth(false);
        var leftPanelPaddingRight = parseInt(leftPanelElement.css("padding-right"));
        var leftPanelPaddingLeft = parseInt(leftPanelElement.css("padding-left"));
        
        $(element).css("width", scope.getWindowWidth() - leftPanelWidth - leftPanelPaddingRight - leftPanelPaddingLeft + "px");
      }
    };
  };
  
  Directive.$inject = ["$window"];
  
  module
    .directive('fillWidth', Directive);
  
});