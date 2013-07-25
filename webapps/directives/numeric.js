'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var Directive = function () {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, model) {

        var pattern = attrs['integer'] ? /^-?[\d]+$/ : /^-?[\d]+(.[\d]+)([eE][-+]?[0-9]+)?$/;

        var numberParser = function(value) {

          var isValid = pattern.test(value);
          model.$setValidity('numeric', isValid);

          return parseFloat(value, 10);
        };

        var numberFormatter = function(value) {

          var isValid = pattern.test(value);
          model.$setValidity('numeric', isValid);

          //return value;
          return parseFloat(value, 10);
        };

        model.$parsers.push(numberParser);
      }
    };
  };

  module
    .directive('numeric', Directive);
  
});
