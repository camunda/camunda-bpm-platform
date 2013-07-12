ngDefine('camunda.common.directives', [ 'jquery' ], function(module, $) {

  var Password = function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, model) {
               
        model.$parsers.unshift(function(viewValue) {
        
          var matchedPasswordExpr = attrs.password;
          var matchedPasswordValue = scope.$eval(matchedPasswordExpr);

          if (viewValue.length >= 8 && viewValue == matchedPasswordValue) {
            model.$setValidity('password', true);
          } else {
            model.$setValidity('password', false);            
          }
          return viewValue;
        });
      }
    };
  };
  
  module.directive("password", Password);

});
