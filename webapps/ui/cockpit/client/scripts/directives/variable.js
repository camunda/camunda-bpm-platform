/*
  DEPRECATION WARNING:
  this directive should not be used anymore, see cam-widget-variable instead
*/

'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/variable.html', 'utf8');

var Directive = function() {
  return {
    restrict: 'EAC',
    scope: {
      variable: '='
    },
    replace: true,
    template: template,
    link: function(scope, element) {

      var inPlaceEdit = (element.attr('inline-edit') !== undefined),
          oldVariableValue,
          oldVariableValueBoolean = true;

      scope.autofocus = !!(element.attr('autofocus') !== undefined);

      scope.isBoolean = function(variable) {
        return variable.type.toLowerCase() === 'boolean';
      };

      scope.isInteger = function(variable) {
        return variable.type.toLowerCase() === 'integer';
      };

      scope.isShort = function(variable) {
        return variable.type.toLowerCase() === 'short';
      };

      scope.isLong = function(variable) {
        return variable.type.toLowerCase() === 'long';
      };

      scope.isDouble = function(variable) {
        return variable.type.toLowerCase() === 'double';
      };

      scope.isFloat = function(variable) {
        return variable.type.toLowerCase() === 'float';
      };

      scope.isString = function(variable) {
        return variable.type.toLowerCase() === 'string';
      };

      scope.isDate = function(variable) {
        return variable.type.toLowerCase() === 'date';
      };

      scope.isNull = function(variable) {
        return variable.type.toLowerCase() === 'null';
      };

      scope.isObject = function(variable) {
        return variable.type.toLowerCase() === 'object';
      };

      scope.isInPlaceEdit = function() {
        return inPlaceEdit;
      };

      scope.$watch('variable.type', function(newValue, oldValue) {
        if (oldValue === newValue) {
          return;
        }

        if (newValue.toLowerCase() === 'boolean') {
          oldVariableValue = scope.variable.value;

          scope.variable.value = oldVariableValueBoolean;
          return;
        }

        if (oldValue.toLowerCase() === 'boolean') {
          oldVariableValueBoolean = scope.variable.value;

          scope.variable.value = oldVariableValue;
          return;
        }

        if(newValue.toLowerCase() === 'null') {
          scope.variable.value = null;
        }

        if(newValue.toLowerCase() === 'object') {
          scope.variable.valueInfo = scope.variable.valueInfo || {};
        }
      });

    }
  };
};

module.exports = Directive;
