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

module.exports = [
  '$scope',
  '$http',
  '$filter',
  'Uri',
  'Notifications',
  '$uibModalInstance',
  'processDefinition',
  'fixDate',
  '$translate',
  '$rootScope',
  function(
    $scope,
    $http,
    $filter,
    Uri,
    Notifications,
    $modalInstance,
    processDefinition,
    fixDate,
    $translate,
    $rootScope
  ) {
    var BEFORE_UPDATE = 'BEFORE_UPDATE',
      PERFORM_UPDATE = 'PERFORM_UDPATE',
      UPDATE_SUCCESS = 'SUCCESS',
      UPDATE_FAILED = 'FAIL';

    var dateFilter = $filter('date'),
      dateFormat = "yyyy-MM-dd'T'HH:mm:ss";

    $scope.processDefinition = processDefinition;

    $scope.status = BEFORE_UPDATE;

    $scope.data = {
      includeInstances: true,
      executeImmediately: true,
      executionDate: dateFilter(Date.now(), dateFormat)
    };

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.updateSuspensionState = function() {
      $scope.status = PERFORM_UPDATE;

      var data = {};

      data.suspended = !processDefinition.suspended;
      data.includeProcessInstances = $scope.data.includeInstances;
      data.executionDate = !$scope.data.executeImmediately
        ? fixDate($scope.data.executionDate)
        : null;

      $http
        .put(
          Uri.appUri(
            'engine://engine/:engine/process-definition/' +
              processDefinition.id +
              '/suspended/'
          ),
          data
        )
        .then(function() {
          $scope.status = UPDATE_SUCCESS;

          if ($scope.data.executeImmediately) {
            $rootScope.$broadcast(
              '$processDefinition.suspensionState.changed',
              processDefinition
            );

            Notifications.addMessage({
              status: $translate.instant(
                'PLUGIN_UPDATE_SUSPENSION_STATE_STATUS_FINISHED'
              ),
              message: $translate.instant(
                'PLUGIN_UPDATE_SUSPENSION_STATE_MESSAGE_1'
              ),
              exclusive: true
            });
          } else {
            Notifications.addMessage({
              status: $translate.instant(
                'PLUGIN_UPDATE_SUSPENSION_STATE_STATUS_FINISHED'
              ),
              message: $translate.instant(
                'PLUGIN_UPDATE_SUSPENSION_STATE_MESSAGE_2'
              ),
              exclusive: true
            });
          }
        })
        .catch(function(response) {
          $scope.status = UPDATE_FAILED;
          var errorMessage;
          if ($scope.data.executeImmediately) {
            errorMessage = $translate.instant(
              'PLUGIN_UPDATE_SUSPENSION_STATE_MESSAGE_3',
              {message: response.data.message}
            );
          } else {
            errorMessage = $translate.instant(
              'PLUGIN_UPDATE_SUSPENSION_STATE_MESSAGE_4',
              {message: response.data.message}
            );
          }
          Notifications.addError({
            status: $translate.instant(
              'PLUGIN_UPDATE_SUSPENSION_STATE_STATUS_FINISHED'
            ),
            message: errorMessage,
            exclusive: true
          });
        });
    };

    $scope.isValid = function() {
      var formScope = angular
        .element('[name="updateSuspensionStateForm"]')
        .scope();
      return formScope && formScope.updateSuspensionStateForm
        ? formScope.updateSuspensionStateForm.$valid
        : false;
    };

    $scope.close = function(status) {
      var response = {};

      response.status = status;
      response.suspended = !processDefinition.suspended;
      response.executeImmediately = $scope.data.executeImmediately;
      response.executionDate = $scope.data.executionDate;

      $modalInstance.close(response);
    };
  }
];
