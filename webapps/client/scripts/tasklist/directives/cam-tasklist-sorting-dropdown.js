define([
  'angular',
  'text!./cam-tasklist-sorting-dropdown.html'
], function (
  angular,
  template
) {
  'use strict';
  return ['$translate',
  function ($translate){
    return {
      restrict: 'A',

      replace: true,

      template: template,

      scope: {
        options: '=',
        clickHandler: '&',
        change: '&',
        resetFunction: '='
      },

      controller: [
        '$scope',
      function (
        $scope
      ) {
        $scope.hasOptions = function(){
          return $scope.options && Object.keys($scope.options).length > 0;
        };
      }],

      link: function($scope) {

        $scope.change = $scope.$eval($scope.change);

        // --- CONTROL FUNCTIONS ---
        $scope.resetInputs = {};
        $scope.resetFunction = function(id, type, value){
          if($scope.sortableVariables[id]) {
            $scope.focusedOn = id;
          } else {
            $scope.focusedOn = null;
          }
          // reset all inputs
          for(var key in $scope.resetInputs) {
            if(key === id) {
              $scope.resetInputs[key](type, value);
            } else {
              $scope.resetInputs[key]('Integer', '');
            }
          }
        };

        $scope.handleClick = function(evt, name, type, value) {
          $scope.clickHandler({$event: evt, id: name, type: type, value: value});
        };

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
      }
    };
  }];
});
