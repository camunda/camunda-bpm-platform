define([
  'angular',
  'text!./cam-tasklist-sorting-inputs.html'
], function (
  angular,
  template
) {
  'use strict';
  return [
    '$translate',
  function (
    $translate
  ){
    return {
      restrict: 'AC',

      replace: true,

      template: template,

      scope: {
        change: '=',
        applyHandler: '&',
        resetFunction: '='
      },

      controller: [
        '$scope',
      function (
        $scope
      ) {
        $scope.variableTypes = {
          'Boolean':  $translate.instant('BOOLEAN'),
          'Double':   $translate.instant('DOUBLE'),
          'Date':     $translate.instant('DATE'),
          'Integer':  $translate.instant('INTEGER'),
          'Long':     $translate.instant('LONG'),
          'Short':    $translate.instant('SHORT'),
          'String':   $translate.instant('STRING')
        };
      }],

      link: function (scope, element) {

        scope.resetFunction = function(type, name) {
          scope.variable = name;
          element.find('select').val(type);
        };

        scope.applySorting = function (evt) {
          scope.applyHandler({$event: evt, type: element.find('select').val(), value: scope.variable});
        };
      }
    };
  }];
});
