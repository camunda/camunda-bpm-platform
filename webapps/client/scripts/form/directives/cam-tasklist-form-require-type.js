define(['camunda-bpm-sdk'], function(CamSDK) {
  'use strict';


  return [
    function() {
      return {
        require: 'ngModel',
        link: function($scope, $element, $attrs, ctrl) {
          var validate = function(viewValue) {
            var type = $attrs.requireType;

            if(['Boolean', 'String'].indexOf(type) === -1 && !CamSDK.utils.typeUtils.isType(viewValue, type)) {
              ctrl.$setValidity('requireType', false );
            }
            else {
              ctrl.$setValidity('requireType', true );
            }
            return viewValue;
          };

          ctrl.$parsers.unshift(validate);
          ctrl.$formatters.push(validate);

          $attrs.$observe('requireType', function(comparisonModel){
            return validate(ctrl.$viewValue);
          });
        }};
    }];
});
