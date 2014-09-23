define([
  'text!./cam-form-inline-field.html'
], function(
  template
) {
  'use strict';
  return [
    // '$modal',
  function(
    // $modal
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
        // var modalInstance;

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
          // console.info('echo', fieldScope, arguments.length);
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

        console.info('init inline form field', scope.varType, scope.varValue, scope.editValue);

        scope.$watch('varValue', function(newVal, oldVal) {
          if (oldVal === newVal) { return; }
          console.info('varValue changed', oldVal, newVal);
        });

        scope.$watch('editValue', function(newVal, oldVal) {
          if (oldVal === newVal) { return; }
          console.info('editValue changed', oldVal, newVal);
        });

        console.info('scope', scope);

        // scope.datePickerOptions = {
        //   isOpen: false
        // };


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

          // scope.openDatePicker = function($event) {
          //   $event.preventDefault();
          //   $event.stopPropagation();

          //   scope.datePickerOptions.isOpen = true;
          // };
        }






        scope.toggleEditing = function() {
          // var modal = scope.varType === 'datetime' || scope.varType === 'date';
          // if (modal) {
          //   var modalInstance = $modal.open({
          //     scope: scope,

          //     size: 'sm',

          //     controller: ['$scope', function($scope) {
          //       $scope.$watch('_date', function() {
          //         console.info('date changed', scope._date, $scope._date);
          //       });
          //       $scope.$watch('_time', function() {
          //         console.info('time changed', scope._time, $scope._time);
          //       });
          //       console.info('modal controller', this, arguments);
          //     }],

          //     template: '<datepicker ng-model="_date">'+
          //               (scope.varType === 'datetime' ? '<timepicker ng-model="_time" />' : '')
          //   });
          // }
          // else {
            scope.editing = !scope.editing;
          // }
        };

        // var lastValue = scope.varValue;
        scope.applyChange = function() {
          // // just quit editing if the value didn't change
          // if (lastValue === scope.editValue) {
          //   scope.editing = false;
          //   return;
          // }

          scope.invalid = scope.validator(scope);

          if (!scope.invalid) {
            scope.varValue = scope.editValue;
            // lastValue = scope.varValue;

            console.info('applyChange', scope.varValue);
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
