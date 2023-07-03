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

var template = require('./cam-tasklist-filter-modal-form-variable.html?raw');

var angular = require('camunda-commons-ui/vendor/angular');

var copy = angular.copy;

module.exports = [
  function() {
    return {
      restrict: 'A',
      require: '^camTasklistFilterModalForm',
      scope: {
        filter: '=',
        accesses: '='
      },

      template: template,

      link: function($scope, $element, attrs, parentCtrl) {
        var emptyVariable = {
          name: '',
          label: ''
        };

        $scope.filter.properties.showUndefinedVariable =
          $scope.filter.properties.showUndefinedVariable || false;
        $scope.variables = $scope.filter.properties.variables =
          $scope.filter.properties.variables || [];

        // register handler to show or hide the accordion hint /////////////////

        var showHintProvider = function() {
          for (var i = 0, nestedForm; (nestedForm = nestedForms[i]); i++) {
            var variableName = nestedForm.variableName;
            var variableLabel = nestedForm.variableLabel;

            if (variableName.$dirty && variableName.$invalid) {
              return true;
            }

            if (variableLabel.$dirty && variableLabel.$invalid) {
              return true;
            }
          }

          return false;
        };

        parentCtrl.registerHintProvider('filterVariableForm', showHintProvider);

        // handles each nested form //////////////////////////////////////////////

        var nestedForms = [];
        $scope.addForm = function(_form) {
          nestedForms.push(_form);
        };

        // variables interaction /////////////////////////////////////////////////

        $scope.addVariable = function() {
          var _emptyVariable = copy(emptyVariable);
          $scope.variables.push(_emptyVariable);
        };

        $scope.removeVariable = function(delta) {
          $scope.filter.properties.variables = $scope.variables = parentCtrl.removeArrayItem(
            $scope.variables,
            delta
          );
          nestedForms = parentCtrl.removeArrayItem(nestedForms, delta);
        };
      }
    };
  }
];
