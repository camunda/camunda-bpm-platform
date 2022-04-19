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

var template = require('./cam-tasklist-form-external.html')();

module.exports = [
  '$location',
  function($location) {
    return {
      restrict: 'A',

      require: '^camTasklistForm',

      scope: true,

      template: template,

      link: function($scope, $elment, attrs, formController) {
        formController.notifyFormValidated(true);

        $scope.externalFormUrl = null;
        $scope.EXTERNAL_FORM_NOTE = null;

        $scope.$watch(
          function() {
            return (
              formController.getTasklistForm() && formController.getParams()
            );
          },
          function(value) {
            if (value) {
              var tasklistForm = formController.getTasklistForm();
              var params = formController.getParams();

              var key = tasklistForm.key;

              var taskId = params.taskId;
              var processDefinitionKey = params.processDefinitionKey;

              var queryParam = null;

              if (taskId) {
                queryParam = 'taskId=' + taskId;
                $scope.EXTERNAL_FORM_NOTE = 'TASK_EXTERNAL_FORM_NOTE';
              } else if (processDefinitionKey) {
                queryParam = 'processDefinitionKey=' + processDefinitionKey;
                $scope.EXTERNAL_FORM_NOTE = 'PROCESS_EXTERNAL_FORM_NOTE';
              } else {
                return formController.notifyFormInitializationFailed({
                  message: 'INIT_EXTERNAL_FORM_FAILED'
                });
              }

              var absoluteUrl = $location.absUrl();
              var url = $location.url();

              // remove everthing after '#/', e.g.:
              // '.../#/?task=abc&...' ---> '.../#/'
              absoluteUrl = absoluteUrl.replace(url, '/');

              $scope.externalFormUrl = encodeURI(
                key + '?' + queryParam + '&callbackUrl=' + absoluteUrl
              );

              formController.notifyFormInitialized();
            }
          }
        );

        $scope.$watch(
          function() {
            return formController.getOptions();
          },
          function(options) {
            if (options) {
              options.hideCompleteButton = true;
            }
          }
        );
      }
    };
  }
];
