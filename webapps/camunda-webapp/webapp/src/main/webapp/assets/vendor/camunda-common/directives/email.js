ngDefine('camunda.common.directives', [ 'jquery' ], function(module, $) {

  var Directive = function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, model) {
        
        var EMAIL_REGEX = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        
        model.$parsers.unshift(function(viewValue) {
          if (EMAIL_REGEX.test(viewValue) || viewValue=='') {
            model.$setValidity('email', true);
            return viewValue;
          } else {
            model.$setValidity('email', false);
            return null;
          }
        });
      }
    };
  };

  module.directive("email", Directive);

});
