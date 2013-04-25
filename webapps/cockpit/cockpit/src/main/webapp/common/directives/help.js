"use strict";

define([ "angular", "jquery" ], function(angular, $) {
  
  var module = angular.module("common.directives");
  
  var Directive = function() {
    return {
      restrict: 'A',
      scope : {
        helpText: "@",
        helpTitle: "@",
        helpTextVar: "&",
        helpTitleVar: "&",
        colorInvert: "@"
      },
      template: '<span ng-transclude></span><span class="help-toggle"><i class="icon-question-sign" ng-class="colorInvertCls()"></i></span>',
      transclude: true,
      link: function(scope, element, attrs) {
        var help = attrs.helpText || scope.helpTextVar,
            helpTitle = attrs.helpTitle || scope.helpTitleVar,
            colorInvert = !!attrs.colorInvert;

        scope.colorInvertCls = function() {
          return (colorInvert ? 'icon-white' : '');
        };

        var p = "right";
        if(attrs.helpPlacement) {
          p = scope.$eval(attrs.helpPlacement);
        }

        $(element).find(".help-toggle").popover({content: help, title: helpTitle, delay: { show: 0, hide: 0 }, placement: p});
      }
    };
  };
  
  module
    .directive("help", Directive);

});
