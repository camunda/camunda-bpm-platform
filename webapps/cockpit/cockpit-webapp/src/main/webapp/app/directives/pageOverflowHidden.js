"use strict";

define([ "angular", "jquery"], function(angular, $) {
  
  var module = angular.module("cockpit.directives");
  
  var Directive = function () {
    return {
      restrict: 'A',
      link: function(scope, element, attrs, $destroy) {

        $("body").css("overflow", "hidden");
        
      }
    };
  };
  
  module
    .directive('pageOverflowHidden', Directive);
  
});