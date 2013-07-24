'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var Directive = function () {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, model) {
      
        var pattern = /^(\d{2}|\d{4})(?:\-)([0]{1}\d{1}|[1]{1}[0-2]{1})(?:\-)([0-2]{1}\d{1}|[3]{1}[0-1]{1})T(?:\s)?([0-1]{1}\d{1}|[2]{1}[0-3]{1}):([0-5]{1}\d{1}):([0-5]{1}\d{1})?$/;

        var dateParser = function(value) {

          var isValid = pattern.test(value);
          model.$setValidity('date', isValid);

          return value;
        };

        model.$parsers.push(dateParser);
      }
    };
  };

  module
    .directive('date', Directive);  
});
