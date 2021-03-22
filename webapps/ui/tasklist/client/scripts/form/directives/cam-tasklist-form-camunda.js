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
const createForm = require('@bpmn-io/form-js-viewer').createForm;

var angular = require('../../../../../../camunda-commons-ui/vendor/angular');

module.exports = [
  'camAPI',
  '$translate',
  'Notifications',
  'unfixDate',
  function(camAPI, $translate, Notifications, unfixDate) {
    return {
      restrict: 'A',

      require: '^camTasklistForm',

      scope: true,

      template: '',

      link: function($scope, $element, attrs, formController) {
        const Task = camAPI.resource('task');
        const ProcessDefinition = camAPI.resource('process-definition');
        const taskId = formController.getParams().taskId;
        const savedState = JSON.parse(
          localStorage.getItem(`camunda-form:${taskId}`) || '{}'
        );
        let form;
        let variables = [];
        let triggerSubmit = true;

        formController.notifyFormValidated(false);

        const loadVariables = function() {
          if (!taskId) {
            return;
          }

          return Task.formVariables({
            id: taskId,
            deserializeValues: false
          })
            .then(result => {
              angular.forEach(result, function(value, name) {
                var parsedValue = value.value;

                if (value.type === 'Date') {
                  parsedValue = unfixDate(parsedValue);
                }
                variables.push({
                  name: name,
                  value: parsedValue,
                  type: value.type,
                  fixedName: true
                });
              });
            })
            .catch(err => {
              return $translate('LOAD_VARIABLES_FAILURE')
                .then(function(translated) {
                  Notifications.addError({
                    status: translated,
                    message: err.message,
                    scope: $scope
                  });
                })
                .catch(angular.noop);
            });
        };

        function renderForm(schema) {
          const data = variables.reduce((res, variable) => {
            res[variable.name] = variable.value;
            return res;
          }, {});

          form = createForm({
            container: $element[0],
            schema,
            data: {...data, ...savedState}
          });
          formController.notifyFormInitialized();

          form.on('submit', () => {
            if (triggerSubmit) {
              formController.attemptComplete();
            }
          });

          form.on('changed', evt => {
            formController.notifyFormDirty(true);

            const hasError = !!Object.keys(evt.errors).length;
            formController.notifyFormValidated(hasError);
          });
        }

        function getFormData() {
          triggerSubmit = false;
          const res = form.submit();
          triggerSubmit = true;

          return res;
        }

        function handleSave() {
          const {data} = getFormData();
          localStorage.setItem(`camunda-form:${taskId}`, JSON.stringify(data));
          formController.notifyFormDirty(false);
        }

        function clearSave() {
          localStorage.removeItem(`camunda-form:${taskId}`);
        }

        formController.registerSaveHandler(handleSave);

        $scope.$on('authentication.login.required', handleSave);

        async function handleSubmit() {
          const {data, errors} = getFormData();
          if (Object.keys(errors).length) {
            throw errors;
          }
          const variablePayload = Object.entries(data).reduce(
            (res, [key, value]) => {
              res[key] = {value};
              return res;
            },
            {}
          );

          if (taskId) {
            return await Task.submitForm({
              id: taskId,
              variables: variablePayload
            });
          } else {
            return await ProcessDefinition.submitForm({
              id: formController.getParams().processDefinitionId,
              variables: variablePayload
            });
          }
        }

        formController.registerCompletionHandler(async cb => {
          try {
            const result = await handleSubmit();
            clearSave();
            cb(null, result);
          } catch (err) {
            cb(err);
          }
        });

        function handleAsynchronousFormKey(formInfo) {
          fetch(formInfo.key + `?noCache=${Date.now()}`)
            .then(async res => {
              if (res.status !== 200) {
                throw new Error(res.statusText);
              }

              const json = await res.json();

              await loadVariables();
              renderForm(json);
            })
            .catch(err => {
              formController.notifyFormInitializationFailed(err);
            });
        }

        $scope.$watch('asynchronousFormKey', handleAsynchronousFormKey, true);
      }
    };
  }
];
