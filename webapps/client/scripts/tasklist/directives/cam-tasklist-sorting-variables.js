define([
  'angular',
  'text!./cam-tasklist-sorting-variables.html'
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
      restrict: 'C',

      replace: true,

      require: '^cam-sorting-choices',

      template: template,

      scope: {
        sorting: '='
      },

      controller: [
        '$scope',
      function (
        $scope
      ) {
        $scope.focusedOn = null;
        if ($scope.sorting) {
          $scope.focusedOn = $scope.sorting.by;
        }

        $scope.sortableVariables = {
          processVariable:        $translate.instant('PROCESS_VARIABLE'),
          executionVariable:      $translate.instant('EXECUTION_VARIABLE'),
          taskVariable:           $translate.instant('TASK_VARIABLE'),
          caseExecutionVariable:  $translate.instant('CASE_EXECUTION_VARIABLE'),
          caseInstanceVariable:   $translate.instant('CASE_INSTANCE_VARIABLE')
        };

        $scope.showInputs = function ($event, name) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.focusedOn = name;
        };
      }]
    };
  }];
});
