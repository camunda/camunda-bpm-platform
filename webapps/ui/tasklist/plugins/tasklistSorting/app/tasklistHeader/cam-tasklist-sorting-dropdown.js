'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-sorting-dropdown.html', 'utf8');

module.exports = ['$translate',
  function($translate) {
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

      link: function($scope) {

        $scope.change = $scope.$eval($scope.change);

        $scope.variable = {
          varName: '',
          varType: 'Integer'
        };


        $scope.hasOptions = function() {
          return $scope.options && Object.keys($scope.options).length > 0;
        };

        // --- CONTROL FUNCTIONS ---
        $scope.resetInputs = {};
        $scope.resetFunction = function(id, type, value) {
          if($scope.sortableVariables[id]) {
            $scope.focusedOn = id;
            $scope.variable.varType = type;
            $scope.variable.varName = value;
          } else {
            $scope.focusedOn = null;
            $scope.variable.varType = 'Integer';
            $scope.variable.varName = '';
          }
        };

        $scope.handleClick = function(evt, name) {
          if($scope.sortableVariables[name]) {
            $scope.clickHandler({$event: evt, id: name, type: $scope.variable.varType, value: $scope.variable.varName});
          } else {
            $scope.clickHandler({$event: evt, id: name});
          }

        };

        $scope.sortableVariables = {
          processVariable:        $translate.instant('PROCESS_VARIABLE'),
          executionVariable:      $translate.instant('EXECUTION_VARIABLE'),
          taskVariable:           $translate.instant('TASK_VARIABLE'),
          caseExecutionVariable:  $translate.instant('CASE_EXECUTION_VARIABLE'),
          caseInstanceVariable:   $translate.instant('CASE_INSTANCE_VARIABLE')
        };

        $scope.showInputs = function($event, name) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.focusedOn = name;
        };
      }
    };
  }];
