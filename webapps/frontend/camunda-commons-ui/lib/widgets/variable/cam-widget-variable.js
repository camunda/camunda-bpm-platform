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

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  varUtils = require('./cam-variable-utils'),
  template = require('./cam-widget-variable.html?raw');

var variableTypes = varUtils.types;

var modalCtrl = varUtils.modalCtrl;

module.exports = [
  '$uibModal',
  function($modal) {
    return {
      template: template,

      scope: {
        variable: '=camVariable',
        // A variable HAS a the following information
        // {
        //   value: <Mixed>,
        //   name: <String>,
        //   type: <String>,
        //
        // A variable MAY HAVE a the following information
        //   id: <UUID>,
        //   valueInfo: {
        //     objectTypeName: <String>,
        //     serializationDataFormat: <String>
        //   },
        //
        //   activitiyInstanceId: <UUID>,
        //   caseExecutionId: <UUID>,
        //   caseInstanceId: <UUID>,
        //   executionId: <UUID>,
        //   processInstanceId: <UUID>,
        //   taskId: <UUID>
        // }
        display: '@?',
        shown: '=?',
        disabled: '=?',
        hiddenTypes: '=?'
      },

      link: function($scope, element) {
        function isShownType(what) {
          if (
            !Array.isArray($scope.hiddenTypes) ||
            !$scope.hiddenTypes.length
          ) {
            return true;
          }
          return $scope.hiddenTypes.indexOf(what) === -1;
        }

        $scope.variableTypes = variableTypes.filter(isShownType);

        var defaultValues = varUtils.defaultValues;

        $scope.isPrimitive = varUtils.isPrimitive($scope);

        $scope.useCheckbox = varUtils.useCheckbox($scope);

        $scope.isShown = function(what) {
          if (!Array.isArray($scope.shown) || !$scope.shown.length) {
            return true;
          }
          return $scope.shown.indexOf(what) > -1;
        };

        $scope.isDisabled = function(what) {
          if (!Array.isArray($scope.disabled) || !$scope.disabled.length) {
            return false;
          }
          return $scope.disabled.indexOf(what) > -1;
        };

        $scope.shownClasses = function() {
          if (!Array.isArray($scope.shown) || !$scope.shown.length) {
            return '';
          }
          return $scope.shown
            .map(function(part) {
              return 'show-' + part;
            })
            .join(' ');
        };
        $scope.$watch('shown', function() {
          element
            .removeClass('show-type show-name show-value')
            .addClass($scope.shownClasses());
        });

        var validate = varUtils.validate($scope);
        $scope.valid = true;
        $scope.$watch('variable.value', validate);
        $scope.$watch('variable.name', validate);
        $scope.$watch('variable.type', validate);
        validate();

        // backup is used to recover a variable value
        // from either type or null switch
        var backup = $scope.variable.value;

        $scope.$watch('variable.type', function(current, previous) {
          // convert the value to boolean when the type becomes Boolean
          if (current === 'Boolean') {
            // we don't do anything if the value is null
            if ($scope.variable.value !== null) {
              backup = $scope.variable.value;

              $scope.variable.value =
                $scope.variable.value === 'false'
                  ? false
                  : !!$scope.variable.value;
            }
          } else if (previous === 'Boolean') {
            $scope.variable.value = backup;
          }

          var classList = element[0].classList;
          if (previous) {
            classList.remove('var-type-' + previous.toLowerCase());
          }
          if (current) {
            classList.add('var-type-' + current.toLowerCase());
          }
        });

        $scope.isNull = function() {
          return $scope.variable.value === null;
        };
        $scope.setNonNull = function() {
          $scope.variable.value = backup || defaultValues[$scope.variable.type];
        };
        $scope.setNull = function() {
          backup = $scope.variable.value;
          $scope.variable.value = null;
        };

        $scope.editVariableValue = function() {
          $modal
            .open({
              template: varUtils.templateDialog, //templateDialog,

              controller: modalCtrl,

              windowClass: 'cam-widget-variable-dialog',

              resolve: {
                variable: function() {
                  return angular.copy($scope.variable);
                },
                readonly: function() {
                  return $scope.display;
                }
              }
            })
            .result.then(function(result) {
              $scope.variable.value = result.value;
              $scope.variable.valueInfo = result.valueInfo;
            })
            .catch(angular.noop);
        };
      }
    };
  }
];
