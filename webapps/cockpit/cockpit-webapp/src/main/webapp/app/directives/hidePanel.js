"use strict";

define([ "angular", "jquery"], function(angular, $) {
  
  var module = angular.module("cockpit.directives");
   
  var hideButtonTemplate = '<div>' + 
                           '  <button type="button" ng-click="hideDiv()" class="arrow-button">' + 
                           '    <i class="icon-chevron-left"></i>' +
                           '  </button>' + 
                           '</div>' + 
                           '<div id="showLeftPanel">' + 
                           '  <button type="button" class="arrow-button" ng-click="showDiv()">' + 
                           '    <i class="icon-chevron-right"></i>' +
                           '  </button>' + 
                           '</div>';
  
  var Directive = function () {
    return {
      restrict: 'A',
      template: hideButtonTemplate,
      link: function(scope, element, attrs, $destroy) {
        
        var leftPanelElement = $(".left-panel");
        var leftPanelWidth = leftPanelElement.outerWidth(false);
        
        var rightPanelElement = $(".right-panel");
        
        var showRightPanelElement = $("#showLeftPanel");
        showRightPanelElement.css("top", leftPanelElement.css("padding-top"));
        showRightPanelElement.css("position", "absolute");
        showRightPanelElement.hide();
        
        scope.hideDiv = function () {
          leftPanelElement
            .animate({
              left: "-=" + leftPanelWidth + "px"
              },
              "slow");
          
          rightPanelElement
            .animate({
              left: "-=" + leftPanelWidth + "px"
              },
              "slow");
          
          $("#processDiagram").removeOverscroll();
          rightPanelElement.css("width", "+=" + leftPanelWidth + "px");
          $("#processDiagram").overscroll({captureWheel:false});
          
          showRightPanelElement.css("left", leftPanelWidth + "px");
          showRightPanelElement.show();
        };
        
        scope.showDiv = function () {
          leftPanelElement
          .animate({
            left: "+=" + leftPanelWidth + "px"
            },
            "slow");
        
          rightPanelElement
            .animate({
              left: "+=" + leftPanelWidth + "px"
              },
              "slow");
          
          $("#processDiagram").removeOverscroll();
          rightPanelElement.css("width", "-=" + leftPanelWidth + "px");
          $("#processDiagram").overscroll({captureWheel:false});
          
          showRightPanelElement.hide();
        };
      }
    };
  };
  
  module
    .directive('hidePanel', Directive);
  
});