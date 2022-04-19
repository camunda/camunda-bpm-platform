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

var template = require('./cam-tasklist-form-generic.html')();

var angular = require('../../../../../../camunda-commons-ui/vendor/angular');
var $ = require('jquery');

module.exports = [
  'CamForm',
  'camAPI',
  '$timeout',
  function(CamForm, camAPI, $timeout) {
    return {
      restrict: 'A',

      require: '^camTasklistForm',

      scope: true,

      template: template,

      link: function($scope, $element, attrs, formController) {
        var formElement = $($element[0]).find('form');
        var camForm = ($scope.camForm = null);
        var form = {
          $valid: false,
          $invalid: true
        };

        var $update = false;

        $scope.$watch(
          function() {
            return $update;
          },
          function(value) {
            if (value) {
              showForm(value, formController.getParams());
              $update = false;
            }
          }
        );

        $scope.$watch(
          function() {
            return formController.getTasklistForm();
          },
          function(value) {
            if (value) {
              $update = true;
              $scope.variables = [];
            }
          }
        );

        $scope.$watch(
          function() {
            return form && form.$valid;
          },
          function(value) {
            formController.notifyFormValidated(!value);
          }
        );

        function showForm(tasklistForm, params) {
          params = angular.copy(params);

          delete params.processDefinitionKey;

          angular.extend(params, {
            client: camAPI,
            formElement: formElement,
            done: done
          });

          $scope.camForm = camForm = new CamForm(params);
        }

        var done = function(err, _camForm) {
          if (err) {
            return formController.notifyFormInitializationFailed(err);
          }
          camForm = _camForm;

          var formName = _camForm.formElement.attr('name');
          var camFormScope = _camForm.formElement.scope();

          if (!camFormScope) {
            return;
          }

          form = camFormScope[formName];
          formController.notifyFormInitialized();

          if ($scope.options.autoFocus) {
            $timeout(function() {
              var focusElement = _camForm.formElement[0].querySelectorAll(
                'input'
              )[0];
              if (focusElement) {
                focusElement.focus();
              }
            });
          }
        };

        function clearVariableManager() {
          var variables = camForm.variableManager.variables;
          for (var v in variables) {
            camForm.variableManager.destroyVariable(v);
          }
        }

        function clearFields() {
          camForm.fields = [];
        }

        var complete = function(callback) {
          function localCallback(error, result) {
            clearVariableManager();
            clearFields();
            return callback(error, result);
          }

          try {
            camForm.initializeFieldHandlers();
          } catch (error) {
            return localCallback(error);
          }

          var variables = camForm.variableManager.variables;
          for (var v in variables) {
            variables[v].value = null;
          }

          $scope.variables
            .filter(el => el.type === 'Object')
            .forEach(el => {
              camForm.variableManager.createVariable({
                name: el.name,
                type: 'Object',
                value: el.value,
                valueInfo: el.valueInfo
              });
            });

          camForm.submit(localCallback);
        };

        formController.registerCompletionHandler(complete);
      }
    };
  }
];
