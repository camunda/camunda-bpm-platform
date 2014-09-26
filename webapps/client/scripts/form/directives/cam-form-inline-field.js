define([
  'text!./cam-form-inline-field.html'
], function(
  template
) {
  'use strict';
  return [function() {
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

        scope.echo = function(fieldScope) {
          scope.editValue = fieldScope.editValue;
        };

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


        function buildDatetime() {
          var d = scope._datetime.date;
          var t = scope._datetime.time;
          scope.editValue.setFullYear(d.getFullYear());
          scope.editValue.setMonth(d.getMonth());
          scope.editValue.setDate(d.getDate());

          scope.editValue.setHours(t.getHours());
          scope.editValue.setMinutes(t.getMinutes());
          scope.editValue.setSeconds(t.getSeconds());
          scope.editValue.setMilliseconds(t.getMilliseconds());
        }

        if (['datetime', 'date', 'time'].indexOf(scope.varType) > -1) {
          scope._datetime = {};
          scope._datetime.date = new Date();
          scope._datetime.time = new Date();

          if (!(scope.editValue instanceof Date))  {
            scope.editValue = new Date(scope.editValue || Date.now());
          }

          scope._datetime.date.setTime(scope.editValue.getTime());
          scope._datetime.time.setTime(scope.editValue.getTime());


          scope.$watch('_datetime.date', buildDatetime);
          scope.$watch('_datetime.time', buildDatetime);
        }






        scope.toggleEditing = function() {
          scope.editing = !scope.editing;
        };

        scope.applyChange = function() {
          scope.invalid = scope.validator(scope);

          if (!scope.invalid) {
            scope.varValue = scope.editValue;

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
      },

      transclude: true
    };
  }];
});
