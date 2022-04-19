/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
var fs = require('fs');

var template = require('./cam-tasklist-sorting-dropdown.html')();

module.exports = [
  '$translate',
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
          if ($scope.sortableVariables[id]) {
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
          if ($scope.sortableVariables[name]) {
            $scope.clickHandler({
              $event: evt,
              id: name,
              type: $scope.variable.varType,
              value: $scope.variable.varName
            });
          } else {
            $scope.clickHandler({$event: evt, id: name});
          }
        };

        $scope.sortableVariables = {
          processVariable: $translate.instant('PROCESS_VARIABLE'),
          executionVariable: $translate.instant('EXECUTION_VARIABLE'),
          taskVariable: $translate.instant('TASK_VARIABLE'),
          caseExecutionVariable: $translate.instant('CASE_EXECUTION_VARIABLE'),
          caseInstanceVariable: $translate.instant('CASE_INSTANCE_VARIABLE')
        };

        $scope.showInputs = function($event, name) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.focusedOn = name;
        };
      }
    };
  }
];
