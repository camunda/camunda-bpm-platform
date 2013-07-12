ngDefine('camunda.common.directives', [ 'angular', 'jquery' ], function(module, angular, $) {

module.directive('requestAware', function(RequestStatus) {    
  return {
      link: function(scope, element, attrs) {

        var DISABLE_AJAX_LOADER = "disableAjaxLoader";

        var formName;
        var showAjaxLoader = true;

        if (attrs.requestAware) {
          var params = attrs.requestAware.split(",");
          formName = $.trim(params[0]);
          if (params[1] && $.trim(params[1]) == DISABLE_AJAX_LOADER) {
            showAjaxLoader = false;
          }
        }

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
  });
});
