ngDefine('camunda.common.directives', [ 'jquery' ], function(module, $) {

  /**
    this directive is placed on an input field and validates whether the 
    password contains at least 8 characters 
  **/
  var Password = function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, model) {
               
        model.$parsers.unshift(function(viewValue) {
        
          if (viewValue && viewValue.length >= 8) {
            model.$setValidity('password', true);
          } else {
            model.$setValidity('password', false);            
          }
          return viewValue;
        });
      }
    };
  };
  
  /** 
    this directive is placed on the Password (repeat) input field. 
    it is configured with the name of the property which holds the password we must repeat.
  **/
  var PasswordRepeat = function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, model) {

        // this is the name of the scope property
        // holding the value of the password we are trying to match.
        var repeatedPasswordName = attrs.passwordRepeat;

        // check match if we are changed
        model.$parsers.unshift(function(viewValue) {
          var repeatedPasswordValue = scope.$eval(repeatedPasswordName);        
          var isValid = (viewValue == repeatedPasswordValue);
          model.$setValidity('passwordRepeat', isValid);          
          return viewValue;
        });

        // check match if password to repeat is changed
        scope.$watch(repeatedPasswordName, function(newValue) {
          var isValid = (newValue == model.$viewValue);
          model.$setValidity('passwordRepeat', isValid);
          if(!isValid) {
            // make sure '$pristine' value is cleared even if the user 
            // hasn't typed anything into the field yet.
            // if we do not clear '$pristine', the 'invalid' CSS rule does not match
            // and model will ne invalid but without visual feedback.
            model.$setViewValue(model.$viewValue);           
          }
        });
            
      }
    };
  };

  module.directive("password", Password);
  module.directive("passwordRepeat", PasswordRepeat);

});
