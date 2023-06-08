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

var angular = require('angular');

var template = require('./variable-add-dialog.html?raw');

var Controller = [
  '$uibModalInstance',
  '$scope',
  'Notifications',
  'Uri',
  'fixDate',
  '$translate',
  'camAPI',
  'resolved',
  function(
    $modalInstance,
    $scope,
    Notifications,
    Uri,
    fixDate,
    $translate,
    camAPI,
    resolved
  ) {
    $scope.isProcessInstance = resolved.type === 'PROCESS_INSTANCE';
    $scope.isBatchOperation = resolved.type === 'BATCH_OPERATION';
    $scope.isMigration = resolved.type === 'MIGRATION';
    $scope.isCaseInstance = resolved.type === 'CASE_INSTANCE';

    $scope.variableTypes = [
      'String',
      'Boolean',
      'Short',
      'Integer',
      'Long',
      'Double',
      'Date',
      'Null',
      'Object',
      'Json',
      'Xml'
    ];

    var newVariable = ($scope.newVariable = {
      name: null,
      type: 'String',
      value: null
    });

    var PERFORM_SAVE = 'PERFORM_SAVE',
      SUCCESS = 'SUCCESS',
      FAIL = 'FAIL';

    var processInstance = camAPI.resource('process-instance');
    var caseInstance = camAPI.resource('case-instance');

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.close = function() {
      $modalInstance.close($scope.status);
    };

    var isValid = ($scope.isValid = function() {
      // that's a pity... I do not get why,
      // but getting the form scope is.. kind of random
      // m2c: it has to do with the `click event`
      // Hate the game, not the player
      var formScope = angular.element('[name="addVariableForm"]').scope();
      return formScope && formScope.addVariableForm
        ? formScope.addVariableForm.$valid
        : false;
    });

    $scope.save = function() {
      if (!isValid()) {
        return;
      }

      $scope.status = PERFORM_SAVE;

      var data = angular.extend({}, newVariable);

      const variables = resolved.variables;
      if (variables && ($scope.isBatchOperation || $scope.isMigration)) {
        for (let i = 0; i < variables.length; i++) {
          if (data.name === variables[i].variable.name) {
            variables.splice(i, 1);
          }
        }

        variables.push({
          variable: data,
          valid: true,
          changed: false,
          showFailures: true
        });

        $scope.$root.$broadcast('variable.added', data.name);

        $scope.status = SUCCESS;
      } else {
        if (data.type === 'Date') {
          data.value = fixDate(data.value);
        }

        var instanceAPI = $scope.isProcessInstance
          ? processInstance
          : caseInstance;
        instanceAPI
          .setVariable(resolved.instanceId, data)
          .then(function() {
            $scope.status = SUCCESS;

            Notifications.addMessage({
              status: $translate.instant(
                'VARIABLE_ADD_MESSAGE_STATUS_FINISHED'
              ),
              message: $translate.instant('VARIABLE_ADD_MESSAGE_MESSAGE_ADD', {
                name: data.name
              }),
              exclusive: true
            });
          })
          .catch(function(data) {
            $scope.status = FAIL;

            Notifications.addError({
              status: $translate.instant(
                'VARIABLE_ADD_MESSAGE_STATUS_FINISHED'
              ),
              message: $translate.instant(
                'VARIABLE_ADD_MESSAGE_MESSAGE_ERROR',
                {
                  message: data.message
                }
              ),
              exclusive: true
            });
          });
      }
    };
  }
];

module.exports = {
  template: template,
  controller: Controller
};
