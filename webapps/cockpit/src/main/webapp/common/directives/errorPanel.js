"use strict";

define([ "angular", "jquery" ], function(angular, $) {
  
  var module = angular.module("common.directives");
  
  var Directive = function (Error, Uri) {
    return {
      link: function(scope, element, attrs, $destroy) {
  
        $(element).addClass("errorPanel");
        
        var errorConsumer = function(error) {
        var html = "<div class=\"alert alert-error\">";
        html += "<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>";
          
          if (error.status && error.config) {
            html += "<strong>"+error.status+":</strong> ";
            html += "<span>"+error.config+"</span>";
          } else {
            html += "An error occured, try refreshing the page.";
          }
          
          html += "</div>";
            
          element.append(html);
        };
        
        Error.registerErrorConsumer(errorConsumer);      
        scope.$on($destroy, function() {
          Error.unregisterErrorConsumer(errorConsumer);
        });
        
      }
    };
  };

  Directive.$inject = ["Error", "Uri"];
  
  module
    .directive("errorPanel", Directive);

});
