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

var template = require('./cam-tasklist-form-generic-variables.html')();

var angular = require('../../../../../../camunda-commons-ui/vendor/angular');

module.exports = [
  'camAPI',
  'Notifications',
  '$translate',
  'unfixDate',
  'Uri',
  function(camAPI, Notifications, $translate, unfixDate, Uri) {
    return {
      restrict: 'A',

      require: '^camTasklistForm',

      template: template,

      link: function($scope, $element, attrs, formController) {
        /**
         * initial setup
         */

        var Task = camAPI.resource('task');
        var ProcessInstance = camAPI.resource('process-instance');
        var CaseInstance = camAPI.resource('case-instance');

        $scope.showBusinessKey = false;

        var emptyVariable = {
          name: '',
          value: '',
          type: ''
        };

        var variableTypes = ($scope.variableTypes = {
          Boolean: 'checkbox', // handled via switch in HTML template
          Integer: 'text',
          Long: 'text',
          Short: 'text',
          Double: 'text',
          String: 'text',
          Date: 'text'
        });

        /**
         * determine type of task and handle business key based on it
         */

        var params = formController.getParams();
        var id = params.processInstanceId || params.caseInstanceId;

        if (!params.processDefinitionId && !params.caseDefinitionId) {
          $scope.showBusinessKey = false;
        } else if (id) {
          $scope.readonly = true;
          var resource = params.processInstanceId
            ? ProcessInstance
            : CaseInstance;
          resource
            .get(id)
            .then(function(res) {
              if (res.businessKey) {
                $scope.showBusinessKey = true;
                $scope.businessKey = res.businessKey;
              }
            })
            .catch(angular.noop);
        } else {
          $scope.showBusinessKey = true;
        }

        /**
         * scope methods
         */

        $scope.$watch('tasklistForm', function() {
          $scope.variablesLoaded = false;
        });

        $scope.addVariable = function() {
          var newVariable = angular.copy(emptyVariable);
          $scope.variables.push(newVariable);
        };

        $scope.removeVariable = function(delta) {
          var vars = [];

          angular.forEach($scope.variables, function(variable, d) {
            if (d != delta) {
              vars.push(variable);
            }
          });

          $scope.variables = vars;
        };

        $scope.getVariableNames = function() {
          return $scope.variables.map(function(variable) {
            return variable.name;
          });
        };

        $scope.loadVariables = function() {
          $scope.variablesLoaded = true;
          Task.formVariables(
            {
              id: formController.getParams().taskId,
              deserializeValues: false
            },
            function(err, result) {
              if (err) {
                $scope.variablesLoaded = false;
                return $translate('LOAD_VARIABLES_FAILURE')
                  .then(function(translated) {
                    Notifications.addError({
                      status: translated,
                      message: err.message,
                      scope: $scope
                    });
                  })
                  .catch(angular.noop);
              }

              var variableAdded = false;
              angular.forEach(result, function(value, name) {
                if (variableTypes[value.type]) {
                  var parsedValue = value.value;

                  if (value.type === 'Date') {
                    parsedValue = unfixDate(parsedValue);
                  }
                  $scope.variables.push({
                    name: name,
                    value: parsedValue,
                    type: value.type,
                    fixedName: true
                  });
                  variableAdded = true;
                }

                if (value.type === 'Object') {
                  $scope.variables.push({
                    name: name,
                    value: value.value,
                    type: value.type,
                    valueInfo: value.valueInfo
                  });
                  variableAdded = true;
                }

                if (value.type === 'File') {
                  variableAdded = true;
                  $scope.variables.push({
                    name: name,
                    type: value.type,
                    downloadUrl: Uri.appUri(
                      'engine://engine/:engine/task/' +
                        formController.getParams().taskId +
                        '/variables/' +
                        name +
                        '/data'
                    ),
                    readonly: true
                  });
                }
              });
              if (!variableAdded) {
                $translate('NO_TASK_VARIABLES')
                  .then(function(translated) {
                    Notifications.addMessage({
                      duration: 5000,
                      status: translated,
                      scope: $scope
                    });
                  })
                  .catch(angular.noop);
              }
            }
          );
        };
      }
    };
  }
];
