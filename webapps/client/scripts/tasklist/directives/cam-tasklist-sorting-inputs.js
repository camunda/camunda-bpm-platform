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

      require: '^cam-sorting-variables',

      template: template,

      scope: {
        sorting: '='
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
        scope.applySorting = function () {
          var $sortingsScope = scope.$parent.$parent.$parent.$parent;
          var index = $sortingsScope.index;
          var op = typeof index === 'number' ? 'update' : 'add';

          function fillSorting() {
            scope.sorting.by                  = scope.$parent.focusedOn;
            scope.sorting.order               = scope.sorting.order || 'desc';
            scope.sorting.parameters          = scope.sorting.parameters || {};
            scope.sorting.parameters.variable = element.find('input').val();
            scope.sorting.parameters.type     = element.find('select').val();
          }

          // updating a sorting
          if (op === 'update') {
            fillSorting();
            $sortingsScope.sorting = scope.sorting;
            $sortingsScope.$parent.openDropdowns[index] = false;
          }

          // adding a new sorting
          else {
            scope.sorting = {};
            fillSorting();
            $sortingsScope.sortings.push(scope.sorting);
          }
        };
      }
    };
  }];
});
