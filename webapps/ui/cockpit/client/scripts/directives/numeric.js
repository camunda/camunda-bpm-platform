  'use strict';

  var Directive = function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, model) {

        var pattern = attrs.integer ? /^-?[\d]+$/ : /^(0|(-?(((0|[1-9]\d*)\.\d+)|([1-9]\d*))))([eE][-+]?[0-9]+)?$/;

        var numberParser = function(value) {

          var isValid = pattern.test(value);
          model.$setValidity('numeric', isValid);

          return isValid ? parseFloat(value, 10) : value;
        };

        model.$parsers.push(numberParser);

        var numberFormatter = function(value) {

          // if the value is not set,
          // then ignore it!
          if (value === undefined || value === null) {
            return;
          }

          // test the pattern
          var isValid = pattern.test(value);
          model.$setValidity('numeric', isValid);

          if (isValid) {
            // if the value is valid, then return the
            // value as a number
            return parseFloat(value, 10);
          } else {
            // if the value is invalid, then
            // set $pristine to false and set $dirty to true,
            // that means the user has interacted with the controller.
            model.$pristine = false;
            model.$dirty = true;

            // add 'ng-dirty' as class to the element
            element.addClass('ng-dirty');

            return value;
          }
        };

        model.$formatters.push(numberFormatter);
      }
    };
  };

  module.exports = Directive;
