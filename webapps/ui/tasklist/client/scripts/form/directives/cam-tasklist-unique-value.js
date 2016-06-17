  'use strict';

  module.exports = [function() {

    return {

      require: 'ngModel',

      link: function($scope, $element, $attrs, ctrl) {

        var validate = function(viewValue) {

          var names = JSON.parse($attrs.camUniqueValue);

          ctrl.$setValidity('camUniqueValue', true );

          if (viewValue) {

            if (ctrl.$pristine) {
              ctrl.$pristine = false;
              ctrl.$dirty = true;
              $element.addClass('ng-dirty');
              $element.removeClass('ng-pristine');
            }

            var nameFound = false;
            for(var i = 0; i < names.length; i++) {
              if(names[i] === viewValue) {
                if(nameFound) {
                  ctrl.$setValidity('camUniqueValue', false );
                  break;
                }
                nameFound = true;
              }
            }
          }
          return viewValue;
        };

        ctrl.$parsers.unshift(validate);
        ctrl.$formatters.push(validate);

        $attrs.$observe('camUniqueValue', function() {
          return validate(ctrl.$viewValue);
        });
      }
    };
  }];
