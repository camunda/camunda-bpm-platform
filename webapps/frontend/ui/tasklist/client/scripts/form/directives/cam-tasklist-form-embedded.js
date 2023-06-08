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

var template = require('./cam-tasklist-form-embedded.html?raw');

var angular = require('camunda-commons-ui/vendor/angular');
var $ = require('jquery');

module.exports = [
  'CamForm',
  'camAPI',
  '$timeout',
  'Uri',
  function(CamForm, camAPI, $timeout, Uri) {
    return {
      restrict: 'A',

      require: '^camTasklistForm',

      scope: true,

      template: template,

      link: function($scope, $element, attrs, formController) {
        var container = $($element[0]).find('.form-container');
        var camForm = null;
        var initialVariables = null;
        var form = ($scope.form = {
          $valid: false,
          $invalid: true
        });

        function clearVariableManager() {
          var variables = camForm.variableManager.variables;
          for (var v in variables) {
            camForm.variableManager.destroyVariable(v);
          }
          // reset initial form variables
          camForm.variableManager.variables = angular.copy(initialVariables);
        }

        function handleAsynchronousFormKey(formInfo) {
          // asynchronousFormKey = formInfo;
          if (formInfo && formInfo.loaded) {
            showForm(container, formInfo, formController.getParams());
          }
          if (formInfo && formInfo.failure) {
            formController.notifyFormInitializationFailed(formInfo.error);
          }
        }

        $scope.$watch('asynchronousFormKey', handleAsynchronousFormKey, true);

        $scope.$watch(
          function() {
            return form && form.$valid;
          },
          function(value) {
            formController.notifyFormValidated(!value);
          }
        );

        // watch for changes in the form
        $scope.$watch(
          function() {
            return form && form.$dirty;
          },
          function(value) {
            formController.notifyFormDirty(value);
          }
        );

        function showForm(container, tasklistForm, params) {
          var formUrl = tasklistForm.key;

          params = angular.copy(params);

          delete params.processDefinitionKey;

          angular.extend(params, {
            urlParams: {
              userId: $scope.$root.authentication.name,
              engineName: Uri.appUri(':engine')
            },
            containerElement: container,
            client: camAPI,
            formUrl: formUrl,
            done: done
          });

          if (params.taskId) {
            // Only add URL Params if they have a value
            params.urlParams.taskId = params.taskId;
          }

          camForm = new CamForm(params);
        }

        var done = function(err, _camForm) {
          if (err) {
            return formController.notifyFormInitializationFailed(err);
          }
          camForm = _camForm;
          initialVariables = angular.copy(camForm.variableManager.variables);

          var formName = _camForm.formElement.attr('name');
          var camFormScope = _camForm.formElement.scope();

          if (!camFormScope) {
            return;
          }

          form = camFormScope[formName];
          form.$setPristine();
          formController.notifyFormInitialized();

          $scope.$root.$broadcast('embedded.form.rendered');
        };

        $scope.$on('shortcut:focusForm', function() {
          if (camForm) {
            var focusElement = camForm.formElement[0].querySelectorAll(
              'input'
            )[0];
            if (focusElement) {
              focusElement.focus();
            }
          }
        });

        $scope.formKeyPressed = function formKeyPressed(e) {
          if (e.ctrlKey && (e.keyCode === 13 || e.keyCode === 10)) {
            return formController.attemptComplete();
          }
        };

        var localCallback = function(callback) {
          return function(err, result) {
            if (err) {
              clearVariableManager();
            }

            return callback(err, result);
          };
        };
        var complete = function(callback) {
          camForm.submit(localCallback(callback));
        };

        var save = function(evt) {
          form.$setPristine();
          camForm.store();

          // manually trigger a mouseleave event to make the tooltip disappear
          evt &&
            $timeout(function() {
              angular.element(evt.target).triggerHandler($.Event('mouseleave'));
            });
        };

        formController.registerCompletionHandler(complete);
        formController.registerSaveHandler(save);

        $scope.$on('authentication.login.required', function() {
          save();
        });
      }
    };
  }
];
