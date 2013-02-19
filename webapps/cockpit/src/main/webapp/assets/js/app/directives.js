'use strict';

/* Directives */

angular
.module('cockpit.directives', [])
.directive("help", function(App) {
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
})
.directive('reqAware', function(RequestStatus) {
  	
  return {	  
    link: function(scope, element, attrs) {
      
      var DISABLE_AJAX_LOADER = "disableAjaxLoader";
      
      var formName;
      var showAjaxLoader = true;
      
      if (attrs.reqAware) {
        var params = attrs.reqAware.split(",");
        formName = $.trim(params[0]);
        if (params[1] && $.trim(params[1]) == DISABLE_AJAX_LOADER) {
          showAjaxLoader = false;
        }
      };
      
      function setFormValidity(valid) {
          var form  = scope[formName];
          if(!!form) {
            form.$setValidity("request", valid);
          }     
        }
                   
      function setFormFieldsDisabled(disable) {
        var formElement = $('form[name="'+formName+'"]');
      	if(disable) {
      	  $(":input", formElement).attr("disabled", "disabled");     	  
      	} else {
      	  $(":input", formElement).removeAttr("disabled");	
      	}
      }
    	
      scope.$watch(RequestStatus.watchBusy, function(newValue) {
        scope.isBusy = newValue;
        if(scope.isBusy) { 
        	
      	  if(!!formName) {
            setFormValidity(false);       
            setFormFieldsDisabled(true);           
          }
        	
          if ($(element).is("button")) {
            if(!formName) {
              $(element).attr("disabled", "disabled");	  
            }
          }
        
        } else {
        	
          if(!!formName) {
        	setFormValidity(true);       
        	setFormFieldsDisabled(false);  
          }
          	  
          if ($(element).is("button")) {
            if(!formName) {
              $(element).removeAttr("disabled");	  
            }
            if (showAjaxLoader) {
          	  $(".icon-loading", element).remove();
            }
          } 
        }
      });   
      
      if($(element).is("button") && showAjaxLoader) {
        $(element)
        .bind({    
          click: function() {
            $(element).append("<i class=\"icon-loading\" style=\"margin-left:5px\"></i>");
          }
        });
      }
    }
  };
})
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
