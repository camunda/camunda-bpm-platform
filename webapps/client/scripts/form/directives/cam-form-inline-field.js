define([
  'text!./cam-form-inline-field.html'
], function(
  template
) {
  'use strict';
  return [function() {
    return {
      scope: {
        varValue:   '=value',
        varType:    '@type',
        validator:  '&validate',
        change:     '&'
      },

      template: template,

      link: function(scope) {
        scope.editing = false;

        scope.editValue = scope.editValue || scope.varValue;
        scope.varType =   scope.varType || 'text';
        scope.validator = scope.validator || function() {};
        scope.change =    scope.change || function() {};
        scope.invalid =   false;

        scope.toggleEditing = function() {
          scope.editing = !scope.editing;
        };

        scope.applyChange = function() {
          // just quit editing if the value didn't change
          if (scope.varValue === scope.editValue) {
            scope.editing = false;
            return;
          }

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
