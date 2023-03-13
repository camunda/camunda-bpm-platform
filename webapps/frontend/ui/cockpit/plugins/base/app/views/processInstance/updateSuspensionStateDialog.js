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

module.exports = [
  '$scope',
  '$http',
  '$filter',
  'Uri',
  'Notifications',
  '$uibModalInstance',
  'processInstance',
  '$translate',
  function(
    $scope,
    $http,
    $filter,
    Uri,
    Notifications,
    $modalInstance,
    processInstance,
    $translate
  ) {
    var BEFORE_UPDATE = 'BEFORE_UPDATE',
      PERFORM_UPDATE = 'PERFORM_UDPATE',
      UPDATE_SUCCESS = 'SUCCESS',
      UPDATE_FAILED = 'FAIL';

    $scope.processInstance = processInstance;

    $scope.status = BEFORE_UPDATE;

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.updateSuspensionState = function() {
      $scope.status = PERFORM_UPDATE;

      var data = {};

      data.suspended = !processInstance.suspended;

      $http
        .put(
          Uri.appUri(
            'engine://engine/:engine/process-instance/' +
              processInstance.id +
              '/suspended/'
          ),
          data
        )
        .then(function() {
          $scope.status = UPDATE_SUCCESS;

          Notifications.addMessage({
            status: $translate.instant('PLUGIN_UPDATE_DIALOG_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_UPDATE_DIALOG_MESSAGES_1'),
            exclusive: true
          });
        })
        .catch(function(data) {
          $scope.status = UPDATE_FAILED;

          Notifications.addError({
            status: $translate.instant('PLUGIN_UPDATE_DIALOG_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_UPDATE_DIALOG_ERROR_1', {
              message: data.data.message
            }),
            exclusive: true
          });
        });
    };

    $scope.close = function(status) {
      var response = {};

      response.status = status;
      response.suspended = !processInstance.suspended;

      $modalInstance.close(response);
    };
  }
];
