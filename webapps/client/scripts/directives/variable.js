'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var template =
    '<div>' +

    '  <!-- handle boolean values -->' +
    '  <div ng-if="isBoolean(variable)">' +
    '    <label class="radio">' +
    '      <input ng-model="variable.value" ng-value="true" type="radio" name="booleanValue">' +
    '      true' +
    '    </label>' +
    '    <label class="radio">' +
    '      <input ng-model="variable.value" ng-value="false" type="radio" name="booleanValue">' +
    '      false' +
    '    </label>' +
    '  </div>' +

    '  <!-- handle integer/short/long values -->' +
    '  <input ng-if="isInteger(variable) || isShort(variable) || isLong(variable)" name="editIntegerValue" type="text" ' +
    '         numeric integer="true" ng-model="variable.value" ng-class="{\'in-place-edit\': isInPlaceEdit() }" ' + 
    '         focus="autofocus" required>' +

    '  <!-- handle double/float values -->' +
    '  <input ng-if="isDouble(variable) || isFloat(variable)" name="editFloatValue" type="text" ' +
    '         numeric ng-model="variable.value" ng-class="{\'in-place-edit\': isInPlaceEdit() }" focus="autofocus" required>' +

    '  <!-- handle string values -->' +
    '  <textarea ng-if="isString(variable) || isNull(variable)" rows="5" ng-model="variable.value" class="variable-type-string"' +
    '            ng-class="{\'in-place-edit\': isInPlaceEdit() }" focus="autofocus" required></textarea>' +

    '  <!-- handle date values -->' +
    '  <input ng-if="isDate(variable)" date name="editDateValue" ng-model="variable.value"' +
    '         type="text" ng-class="{\'in-place-edit\': isInPlaceEdit() }" focus="autofocus" required>' +
    '</div>';

  var Directive = function ($compile) {
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
          return variable.type === 'boolean' || variable.type === 'Boolean';
        };

        var isInteger = scope.isInteger = function (variable) {
          return variable.type === 'integer' || variable.type === 'Integer';
        };

        var isShort = scope.isShort = function (variable) {
          return variable.type === 'short' || variable.type === 'Short';
        };

        var isLong = scope.isLong = function (variable) {
          return variable.type === 'long' || variable.type === 'Long';
        };

        var isDouble = scope.isDouble = function (variable) {
          return variable.type === 'double' || variable.type === 'Double';
        };

        var isFloat = scope.isFloat = function (variable) {
          return variable.type === 'float' || variable.type === 'Float';
        };

        var isString = scope.isString = function (variable) {
          return variable.type === 'string' || variable.type === 'String';
        };

        var isDate = scope.isDate = function (variable) {
          return variable.type === 'date' || variable.type === 'Date';
        };

        var isNull = scope.isNull = function (variable) {
          return variable.type === 'null' || variable.type === 'Null';
        };

        scope.isInPlaceEdit = function () {
          return inPlaceEdit;
        };   

        scope.$watch('variable.type', function (newValue, oldValue) {
          if (oldValue === newValue) {
            return;
          }

          if (newValue === 'boolean' || newValue === 'Boolean') {
            oldVariableValue = scope.variable.value;

            scope.variable.value = oldVariableValueBoolean;
            return;
          }

          if (oldValue === 'boolean' || oldValue === 'Boolean') {
            oldVariableValueBoolean = scope.variable.value;

            scope.variable.value = oldVariableValue;
            return;
          }
        });

      }
    };
  };
  
  module
    .directive('variable', Directive);
  

  var FocusDirective = function ($compile) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {

        var focus = attrs['focus'];

        if (focus) {
          element.focus();
        }

      }
    };
  };
  
  module
    .directive('focus', FocusDirective);
});
