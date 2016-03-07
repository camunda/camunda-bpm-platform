/*
  DEPRECATION WARNING:
  this directive should not be used anymore, see cam-widget-variable instead
*/

'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/variable.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

  var Directive = ['$compile', function ($compile) {
    return {
      restrict: 'EAC',
      scope: {
        variable: '='
      },
      replace: true,
      template: template,
      link: function(scope, element, attrs) {

        var inPlaceEdit = (element.attr('inline-edit') !== undefined),
            oldVariableValue,
            oldVariableValueBoolean = true;

        scope.autofocus = !!(element.attr('autofocus') !== undefined);

        var isBoolean = scope.isBoolean = function (variable) {
          return variable.type.toLowerCase() === 'boolean';
        };

        var isInteger = scope.isInteger = function (variable) {
          return variable.type.toLowerCase() === 'integer';
        };

        var isShort = scope.isShort = function (variable) {
          return variable.type.toLowerCase() === 'short';
        };

        var isLong = scope.isLong = function (variable) {
          return variable.type.toLowerCase() === 'long';
        };

        var isDouble = scope.isDouble = function (variable) {
          return variable.type.toLowerCase() === 'double';
        };

        var isFloat = scope.isFloat = function (variable) {
          return variable.type.toLowerCase() === 'float';
        };

        var isString = scope.isString = function (variable) {
          return variable.type.toLowerCase() === 'string';
        };

        var isDate = scope.isDate = function (variable) {
          return variable.type.toLowerCase() === 'date';
        };

        var isNull = scope.isNull = function (variable) {
          return variable.type.toLowerCase() === 'null';
        };

        var isObject = scope.isObject = function (variable) {
          return variable.type.toLowerCase() === 'object';
        };

        scope.isInPlaceEdit = function () {
          return inPlaceEdit;
        };

        scope.$watch('variable.type', function (newValue, oldValue) {
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
  }];

  module.exports = Directive;
