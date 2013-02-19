'use strict';

angular
.module('cockpit.directive.error.panel', [])
.directive('errorPanel', function(Error, App) {
  return {
    link: function(scope, element, attrs, $destroy) {

      $(element).addClass("errorPanel");
      
      var errorConsumer = function(error) {
      var html = "<div class=\"alert alert-error\">";
      html += "<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>";
        
        if (error.status && error.config) {
          html += "<strong>"+error.status+":</strong> ";
          html += "<span>"+error.config+"</span>";
          if (error.type == 'com.camunda.fox.cycle.exception.CycleMissingCredentialsException') {
            html += "<span>(<a style=\"color: #827AA2;\" href=\"" + App.uri("secured/view/profile") + "\">add user credentials</a>)</span>";
          }
        } else {
          html += "An error occured, try refreshing the page or relogin.";
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
});