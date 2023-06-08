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

var template = require('./cam-tasklist-form.html?raw');

var EMBEDDED_KEY = 'embedded:',
  APP_KEY = 'app:',
  ENGINE_KEY = 'engine:',
  DEPLOYMENT_KEY = 'deployment:',
  CAMUNDA_FORMS_KEY = 'camunda-forms:';

function compact(arr) {
  var a = [];
  for (var ay in arr) {
    if (arr[ay]) {
      a.push(arr[ay]);
    }
  }
  return a;
}

var noop = function() {};

module.exports = function() {
  return {
    restrict: 'A',

    scope: {
      tasklistForm: '=',

      /*
       * current options are:
       * - hideCompleteButton: to hide the complete button inside the form directive
       * - disableCompleteButton: to disable or enable the complete button inside
       *   the form directive
       * - disableForm: to disable or enable the form
       * - disableAddVariableButton: to disable or enable the 'Add Variable' button
       *   inside a generic form
       */
      options: '=',

      /*
       * contains parameter like taskId, processDefinitionId, processDefinitionKey etc.
       */
      params: '=',

      /* will be used to make a callback when the form will be completed */
      onFormCompletionCallback: '&',

      /*
       * will be used to register a completion handler, when the completion
       * will be trigger from the outside of a form
       */
      onFormCompletion: '&',

      /*
       * is a callback which will called when the validation state of the
       * form changes (pass the flag '$invalid').
       */
      onFormValidation: '&'
    },

    template: template,

    controller: [
      '$scope',
      'Uri',
      function($scope, Uri) {
        $scope.taskRemoved = false;
        $scope.$on('taskremoved', function() {
          $scope.taskRemoved = true;
        });

        const apply = () => {
          var phase = $scope.$root.$$phase;
          if (phase !== '$apply' && phase !== '$digest') {
            $scope.$apply();
          }
        };

        // setup //////////////////////////////////////////////////////////////////

        $scope.onFormCompletionCallback =
          $scope.onFormCompletionCallback() || noop;
        $scope.onFormCompletion = $scope.onFormCompletion() || noop;
        $scope.onFormValidation = $scope.onFormValidation() || noop;
        $scope.completionHandler = noop;
        $scope.saveHandler = noop;

        $scope.$loaded = false;
        $scope.completeInProgress = false;

        // handle tasklist form ///////////////////////////////////////////////////

        $scope.$watch('tasklistForm', function(value) {
          $scope.$loaded = false;
          if (value) {
            parseForm(value);
            $scope.taskRemoved = false;
          }
        });

        $scope.asynchronousFormKey = {
          loaded: false,
          failure: false
        };

        var API = this;

        function setAsynchronousFormKey(formKey) {
          $scope.asynchronousFormKey.key = formKey;
          $scope.asynchronousFormKey.loaded = true;
        }

        function parseForm(form) {
          // Form is already parsed
          if (form.type) {
            setAsynchronousFormKey(form.key);
            return;
          }

          var key = form.key,
            camundaFormRef = form.camundaFormRef,
            applicationContextPath = form.contextPath;

          // structure may be [embedded:][app:]formKey
          // structure may be [embedded:][deployment:]formKey

          // structure may be [app:]formKey
          // structure may be [deployment:]formKey

          if (!key && !camundaFormRef) {
            form.type = 'generic';
            return;
          }

          if (camundaFormRef) {
            form.type = 'camunda-forms';

            if ($scope.params.taskId) {
              key = Uri.appUri(
                'engine://engine/:engine/task/' +
                  $scope.params.taskId +
                  '/deployed-form'
              );
            } else {
              key = Uri.appUri(
                'engine://engine/:engine/process-definition/' +
                  $scope.params.processDefinitionId +
                  '/deployed-start-form'
              );
            }

            setAsynchronousFormKey(key);
            form.key = key;

            return;
          }

          if (key.indexOf(EMBEDDED_KEY) === 0) {
            key = key.substring(EMBEDDED_KEY.length);
            form.type = 'embedded';
          } else if (key.indexOf(CAMUNDA_FORMS_KEY) === 0) {
            key = key.substring(CAMUNDA_FORMS_KEY.length);
            form.type = 'camunda-forms';
          } else {
            form.type = 'external';
          }

          if (key.indexOf(APP_KEY) === 0) {
            if (applicationContextPath) {
              key = compact([
                applicationContextPath,
                key.substring(APP_KEY.length)
              ])
                .join('/')
                // prevents multiple "/" in the URI
                .replace(/\/([/]+)/, '/');
              setAsynchronousFormKey(key);
            } else {
              API.notifyFormInitializationFailed({
                message: 'EMPTY_CONTEXT_PATH'
              });
            }
          } else if (key.indexOf(DEPLOYMENT_KEY) === 0) {
            if ($scope.params.taskId) {
              key = Uri.appUri(
                'engine://engine/:engine/task/' +
                  $scope.params.taskId +
                  '/deployed-form'
              );
            } else {
              key = Uri.appUri(
                'engine://engine/:engine/process-definition/' +
                  $scope.params.processDefinitionId +
                  '/deployed-start-form'
              );
            }

            setAsynchronousFormKey(key);
          } else if (key.indexOf(ENGINE_KEY) === 0) {
            // resolve relative prefix
            key = Uri.appUri(key);
            setAsynchronousFormKey(key);
          } else {
            setAsynchronousFormKey(key);
          }

          form.key = key;
        }

        // completion /////////////////////////////////////////////

        var completionCallback = function(err, result) {
          $scope.onFormCompletionCallback(err, result);
          $scope.completeInProgress = false;
        };

        var complete = ($scope.complete = function() {
          $scope.completeInProgress = true;
          $scope.completionHandler(completionCallback);
        });

        $scope.onFormCompletion(complete);

        $scope.showCompleteButton = function() {
          return (
            $scope.options &&
            !$scope.options.hideCompleteButton &&
            $scope.$loaded
          );
        };

        var disableCompleteButton = ($scope.disableCompleteButton = function() {
          return (
            $scope.taskRemoved ||
            $scope.completeInProgress ||
            $scope.$invalid ||
            ($scope.options && $scope.options.disableCompleteButton)
          );
        });

        var attemptComplete = function attemptComplete() {
          var canComplete = !disableCompleteButton();
          return canComplete && complete();
        };

        // save ///////////////////////////////////////////////////

        $scope.save = function(evt) {
          $scope.saveHandler(evt);
        };

        // API ////////////////////////////////////////////////////

        this.notifyFormInitialized = function() {
          $scope.$loaded = true;

          apply();
        };

        this.notifyFormInitializationFailed = function(error) {
          $scope.tasklistForm.$error = error;
          // mark the form as initialized
          this.notifyFormInitialized();
          // set the '$invalid' flag to true to
          // not be able to complete a task (or start
          // a process)
          this.notifyFormValidated(true);
        };

        this.notifyFormCompleted = function(err) {
          $scope.onFormCompletion(err);
        };

        this.notifyFormValidated = function(invalid) {
          $scope.$invalid = invalid;
          $scope.onFormValidation(invalid);
          apply();
        };

        this.notifyFormDirty = function(dirty) {
          $scope.$dirty = dirty;
          apply();
        };

        this.getOptions = function() {
          return $scope.options || {};
        };

        this.getTasklistForm = function() {
          return $scope.tasklistForm;
        };

        this.getParams = function() {
          return $scope.params || {};
        };

        this.registerCompletionHandler = function(fn) {
          $scope.completionHandler = fn || noop;
        };

        this.registerSaveHandler = function(fn) {
          $scope.saveHandler = fn || noop;
        };

        this.attemptComplete = attemptComplete;
      }
    ]
  };
};
