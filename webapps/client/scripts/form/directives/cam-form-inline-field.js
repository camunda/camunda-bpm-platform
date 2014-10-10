define([
  'text!./cam-form-inline-field.html'
], function(
  template
) {
  'use strict';

  return [
    '$timeout',
  function(
    $timeout
  ) {

    return {
      scope: {
        varValue:       '=value',
        varType:        '@type',
        validator:      '&validate',
        change:         '&',

        placeholder:    '@',
        inputFormat:    '@',
        displayFormat:  '@',
        icon:           '@',
        suffixed:       '@'
      },

      template: template,

      link: function(scope, element) {
        function isDate() {
          return ['datetime', 'date', 'time'].indexOf(scope.varType) > -1;
        }

        function reset() {
          scope.editing =       false;
          scope.invalid =       false;
          scope.editValue =     scope.varValue;

          scope.validator =     scope.validator ||     function() {};
          scope.change =        scope.change ||        function() {};
          scope.inputFormat =   scope.inputFormat ||   'X';
          scope.displayFormat = scope.displayFormat || 'LLL';
          scope.icon =          scope.icon ||          false;
          scope.suffixed =      scope.suffixed ||      false;

          scope.varType =       !!scope.varType ? scope.varType : 'text';

          scope.simpleField = [
            'color',
            'email',
            'month',
            'number',
            'range',
            'tel',
            'text',
            'time',
            'url',
            'week'
          ].indexOf(scope.varType) > -1;

          if (isDate()) {
            var dateStr = scope.varValue;
            var dateObj = new Date(dateStr ? Date.parse(dateStr) : Date.now());
            dateObj.setTime(dateObj.getTime() + (dateObj.getTimezoneOffset() * 60000));

            scope.dateValue = dateObj;
          }
        }

        scope.startEditing = function() {
          reset();

          scope.editing = true;

          $timeout(function(){
            angular.element('[ng-model="editValue"]').focus();
          }, 100);
        };

        scope.applyChange = function() {

          scope.invalid = scope.validator(scope);

          if (!scope.invalid) {
            if(scope.simpleField) {
              scope.editValue = angular.element('[ng-model="editValue"]').val();
              scope.varValue = scope.editValue;
            }
            else if (isDate()) {
              var offset = scope.dateValue.getTimezoneOffset() * 60000;
              scope.dateValue.setTime(scope.dateValue.getTime() - offset);

              // this is important --------------------vvvvvvvvvvvvvvvvvvv
              scope.varValue = scope.editValue.toJSON().split('.').shift();
            }

            scope.$emit('change', scope.varValue);
            scope.change(scope);

            scope.editing = false;
          }
          else {
            scope.$emit('error', scope.invalid);
          }
        };

        scope.cancelChange = function() {
          scope.editing = false;
        };

        scope.changeDate = function(pickerScope) {
          scope.editValue = scope.dateValue = pickerScope.dateValue;
        };

        scope.$watch('varValue', reset);
      },

      transclude: true
    };
  }];
});
