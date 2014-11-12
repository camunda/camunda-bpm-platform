define(['camunda-bpm-sdk'], function(CamSDK) {
  'use strict';


  return [
    function() {
      return {
        require: 'ngModel',
        link: function($scope, $element, $attrs, ctrl) {
          var validate = function(viewValue) {
            var type = $attrs.camVariableType;

            if(['Boolean', 'String'].indexOf(type) === -1 && !CamSDK.utils.typeUtils.isType(viewValue, type)) {
              ctrl.$setValidity('camVariableType', false );
            }
            else {
              ctrl.$setValidity('camVariableType', true );
            }
            return viewValue;
          };

          ctrl.$parsers.unshift(validate);
          ctrl.$formatters.push(validate);

          $attrs.$observe('camVariableType', function(comparisonModel){
            return validate(ctrl.$viewValue);
          });
        }};
    }];
});
