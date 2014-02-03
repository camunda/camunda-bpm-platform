ngDefine('camunda.common.directives', [ 'angular', 'jquery' ], function(module, angular, $) {

  var requestAwareDirective = [ function() {    
    return {
      require: 'form',

      link: function(scope, element, attrs, ngForm) {

        function setFormValidity(valid) {
          ngForm.$setValidity("request", valid);
        }

        function setFormFieldsEnabled(enabled) {
          var inputs = $(":input", element);

          if (!enabled) {
            inputs.attr("disabled", "disabled");
          } else {
            inputs.removeAttr("disabled");
          }
        }

        function setFormEnabled(enabled) {
          setFormFieldsEnabled(enabled);
          setFormValidity(enabled);
        }

        ngForm.$load = {
          start: function() {
            scope.$broadcast('formLoadStarted');
          },

          finish: function() {
            scope.$broadcast('formLoadFinished');
          }
        };

        scope.$on('formLoadStarted', function() {
          setFormEnabled(false);
        });
        
        scope.$on('formLoadFinished', function() {
          setFormEnabled(true);
        });

        if (attrs.requestAware != 'manual') {
          scope.$on('requestStarted', function() {
            ngForm.$load.start();
          });

          scope.$on('requestFinished', function() {
            ngForm.$load.finish();
          });
        }
      }
    };
  }];

  module.directive('requestAware', requestAwareDirective);
});
