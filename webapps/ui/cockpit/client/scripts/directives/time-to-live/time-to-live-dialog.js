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

const fs = require('fs');
const template = fs.readFileSync(
  __dirname + '/time-to-live-dialog.html',
  'utf8'
);

const Controller = [
  'camAPI',
  '$uibModalInstance',
  '$scope',
  'Notifications',
  '$translate',
  function(camAPI, $modalInstance, $scope, Notifications, $translate) {
    const resource = camAPI.resource($scope.resource);
    $scope.status = null;
    $scope.mode = 'UPDATE';
    $scope.ttl = $scope.definition.historyTimeToLive;

    $scope.isValid = () => {
      return !($scope.mode === 'UPDATE' && $scope.ttl == null);
    };

    $scope.save = () => {
      if (!$scope.isValid()) return;

      $scope.ttl = $scope.mode === 'REMOVE' ? null : $scope.ttl;
      resource
        .updateHistoryTimeToLive($scope.definition.id, {
          historyTimeToLive: $scope.ttl
        })
        .then(function() {
          $scope.status = 'SUCCESS';
          $scope.definition.historyTimeToLive = $scope.ttl;
          customOnChange();

          Notifications.addMessage({
            status: $translate.instant('TIME_TO_LIVE_POPUP_STATE_STATUS'),
            message: $translate.instant('TIME_TO_LIVE_POPUP_STATE_SUCCESS')
          });
        })
        .catch(function(error) {
          $scope.status = 'FAIL';
          $scope.definition.historyTimeToLive = lastValue;

          Notifications.addError({
            status: $translate.instant('PAGES_STATUS_COMMUNICATION_ERROR'),
            message: error
          });
        });
    };

    const customOnChange = () => {
      if (typeof $scope.customOnChange === 'function') {
        $scope.customOnChange();
      }
    };

    const getAndCorrectTimeToLiveValue = () => {
      if ($scope.definition.historyTimeToLive === null) {
        return null;
      }

      return +$scope.definition.historyTimeToLive;
    };

    $scope.close = res => $modalInstance.close(res ? res : $scope.status);

    let lastValue = getAndCorrectTimeToLiveValue();
  }
];

module.exports = {
  template: template,
  controller: Controller
};
