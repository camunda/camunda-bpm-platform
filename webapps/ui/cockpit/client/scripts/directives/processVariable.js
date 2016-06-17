  'use strict';

  module.exports = ['Variables', function(Variables) {

    return {

      require: 'ngModel',
      link: function(scope, element, attrs, ngModel) {

        function parseText(text) {
          var variable;

          try {
            variable = Variables.parse(text);
          } catch (e) {
            // ok, failed to parse variable
          }

          ngModel.$setValidity('processVariableFilter', !!variable);
          return variable;
        }

        ngModel.$parsers.push(parseText);
        ngModel.$formatters.push(Variables.toString);
      }
    };

  }];
