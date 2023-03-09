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

const moment = require('moment');

module.exports = [
  '$scope',
  '$location',
  'Notifications',
  'JobResource',
  '$uibModalInstance',
  'incident',
  '$translate',
  'fixDate',
  function(
    $scope,
    $location,
    Notifications,
    JobResource,
    $modalInstance,
    incident,
    $translate,
    fixDate
  ) {
    var FINISHED = 'finished',
      PERFORM = 'performing',
      FAILED = 'failed';

    $scope.radio = {value: 'preserveDueDate'};
    $scope.dueDate = moment().format('YYYY-MM-DDTHH:mm:00');

    const checkDateFormat = ($scope.checkDateFormat = () =>
      /(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:.(\d\d\d)| )?$/.test(
        $scope.dueDate
      ));

    $scope.checkRetryDisabled = () =>
      $scope.status === PERFORM ||
      ($scope.radio.value === 'dueDate' &&
        (!$scope.dueDate || ($scope.dueDate && !checkDateFormat())));

    $scope.changeDueDate = dueDate => {
      $scope.dueDate = dueDate;
    };

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.incrementRetry = function() {
      $scope.status = PERFORM;

      let payload = {
        retries: 1
      };

      if ($scope.radio.value === 'dueDate') {
        payload = {...payload, dueDate: fixDate($scope.dueDate)};
      }

      JobResource.setRetries(
        {
          id: incident.configuration
        },
        payload,
        function() {
          $scope.status = FINISHED;

          Notifications.addMessage({
            status: $translate.instant('PLUGIN_JOB_RETRY_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_JOB_RETRY_MESSAGE_1'),
            exclusive: true
          });
        },
        function(error) {
          $scope.status = FAILED;
          Notifications.addError({
            status: $translate.instant('PLUGIN_JOB_RETRY_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_JOB_RETRY_ERROR_1', {
              message: error.data.message
            }),
            exclusive: true
          });
        }
      );
    };

    $scope.close = function(status) {
      $modalInstance.close(status);
    };
  }
];
