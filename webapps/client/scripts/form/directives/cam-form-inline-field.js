define([
  'text!./cam-form-inline-field.html'
], function(
  template
) {
  'use strict';

  return [
    '$timeout',
    '$filter',
    '$document',
  function(
    $timeout,
    $filter,
    $document
  ) {

    return {
      scope: {
        varValue:       '=value',
        varType:        '@type',
        validator:      '&validate',
        change:         '&',
        onStart:        '&onStartEditing',
        onCancel:       '&onCancelEditing',

        placeholder:    '@',
        inputFormat:    '@',
        displayFormat:  '@',
        icon:           '@',
        suffixed:       '@',
        options:        '=?'
      },

      template: template,

      link: function(scope, element) {

        var dateFilter = $filter('date'),
            dateFormat = 'yyyy-MM-dd\'T\'HH:mm:ss';

        scope.$on('$locationChangeSuccess', function() {
          scope.cancelChange();
        });

        function fixDateTimezone(dateObj) {
          var time = dateObj.getTime();
          var localOffset = dateObj.getTimezoneOffset() * 60000;
          time = time + localOffset;
          dateObj.setTime(time);
        }

        function isDate() {
          return ['datetime', 'date', 'time'].indexOf(scope.varType) > -1;
        }

        function reset() {
          scope.editing =       false;
          scope.invalid =       false;
          scope.editValue =     scope.varValue;

          scope.validator =     scope.validator ||     function() {};
          scope.onStart =       scope.onStart ||       function() {};
          scope.onCancel =      scope.onCancel ||       function() {};
          scope.change =        scope.change ||        function() {};
          scope.inputFormat =   scope.inputFormat ||   'X';
          scope.displayFormat = scope.displayFormat || 'LLL';
          scope.icon =          scope.icon ||          false;
          scope.suffixed =      scope.suffixed ||      false;
          scope.options =       scope.options ||       [];

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
            var dateStr = scope.varValue,
                dateObj = null;

            if (dateStr) {
              dateObj = new Date(dateStr);
              fixDateTimezone(dateObj);
            } else {
              dateObj = Date.now();
            }

            scope.dateValue = dateObj;
          }
        }

        function stopEditing(evt) {
          if(!scope.editing) {
            return;
          }

          if(element[0].contains(evt.target)) {
            return;
          }

          var targetElement = $(evt.target),
              expectedClasses = 'ng-binding text-muted';

          if (targetElement.hasClass(expectedClasses)) {
            return;
          }

          var children = targetElement.children();
          if (children.hasClass(expectedClasses)) {
            return;
          }

          scope.$apply(scope.cancelChange);
        }

        scope.startEditing = function() {
          if(!scope.editing) {
            reset();

            scope.editing = true;
            scope.onStart(scope);

            $timeout(function(){
              angular.element('[ng-model="editValue"]').focus();
              $document.bind('click', stopEditing);
            }, 100);
          }
        };

        scope.applyChange = function(selection) {

          scope.invalid = scope.validator(scope);

          if (!scope.invalid) {
            if(scope.simpleField) {
              scope.editValue = angular.element('[ng-model="editValue"]').val();
              scope.varValue = scope.editValue;
            }
            else if (scope.varType === "option") {
              scope.editValue = selection;
              scope.varValue = scope.editValue;
            }
            else if (isDate()) {
              scope.varValue = dateFilter(scope.dateValue, dateFormat);
            }

            scope.change(scope);

            scope.editing = false;
            $document.unbind('click', stopEditing);
          }
        };

        scope.cancelChange = function() {
          scope.editing = false;
          scope.onCancel(scope);
          $document.unbind('click', stopEditing);
        };

        scope.changeDate = function(pickerScope) {
          scope.editValue = scope.dateValue = pickerScope.dateValue;
        };

      },

      transclude: true
    };
  }];
});
