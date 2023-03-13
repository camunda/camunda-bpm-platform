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
  '$location',
  'Notifications',
  'ProcessInstanceResource',
  '$uibModalInstance',
  'processInstance',
  'processData',
  'Views',
  '$translate',
  'configuration',
  function(
    $scope,
    $location,
    Notifications,
    ProcessInstanceResource,
    $modalInstance,
    processInstance,
    processData,
    Views,
    $translate,
    configuration
  ) {
    var BEFORE_CANCEL = 'beforeCancellation',
      PERFORM_CANCEL = 'performCancellation',
      CANCEL_SUCCESS = 'cancellationSuccess',
      CANCEL_FAILED = 'cancellationFailed';

    var SKIP_CUSTOM_LISTENERS = configuration.getSkipCustomListeners();
    var SKIP_IO_MAPPINGS = ($scope.SKIP_IO_MAPPINGS = configuration.getSkipIoMappings());

    $scope.processInstance = processInstance;

    var cancelProcessInstanceData = processData.newChild($scope);

    $scope.options = {
      skipCustomListeners: SKIP_CUSTOM_LISTENERS.default,
      skipIoMappings: SKIP_IO_MAPPINGS.default
    };

    $scope.hideSkipCustomListeners = SKIP_CUSTOM_LISTENERS.hidden;

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    cancelProcessInstanceData.provide('subProcessInstances', function() {
      return ProcessInstanceResource.query(
        {
          firstResult: 0,
          maxResults: 5
        },
        {
          superProcessInstance: processInstance.id
        }
      ).$promise;
    });

    cancelProcessInstanceData.provide('subProcessInstancesCount', function() {
      return ProcessInstanceResource.count({
        superProcessInstance: processInstance.id
      }).$promise;
    });

    cancelProcessInstanceData.observe(
      ['subProcessInstancesCount', 'subProcessInstances'],
      function(subProcessInstancesCount, subProcessInstances) {
        $scope.subProcessInstancesCount = subProcessInstancesCount.count;
        $scope.subProcessInstances = subProcessInstances;

        $scope.status = BEFORE_CANCEL;
      }
    );

    $scope.cancelProcessInstance = function() {
      $scope.status = PERFORM_CANCEL;

      $scope.processInstance.$delete(
        $scope.options,
        function() {
          // success
          $scope.status = CANCEL_SUCCESS;
          Notifications.addMessage({
            status: $translate.instant('PLUGIN_CANCEL_PROCESS_STATUS_DELETED'),
            message: $translate.instant('PLUGIN_CANCEL_PROCESS_MESSAGE_1')
          });
        },
        function(err) {
          // failure
          $scope.status = CANCEL_FAILED;
          Notifications.addError({
            status: $translate.instant('PLUGIN_CANCEL_PROCESS_STATUS_FAILED'),
            message: $translate.instant('PLUGIN_CANCEL_PROCESS_MESSAGE_2', {
              message: err.data.message
            }),
            exclusive: ['type']
          });
        }
      );
    };

    $scope.close = function(status) {
      $modalInstance.close(status);

      // if the cancellation of the process instance was successful,
      if (status === CANCEL_SUCCESS) {
        var historyProvider = Views.getProvider({
          id: 'history',
          component: 'cockpit.processInstance.view'
        });

        var path;
        var search;

        if (historyProvider) {
          // redirect to the corresponding historic process instance view
          // keep search params
          search = $location.search();
          path = '/process-instance/' + processInstance.id + '/history';
        } else {
          // or redirect to the corresponding process definition overview.
          path = '/process-definition/' + processInstance.definitionId;
        }

        $location.path(path);
        $location.search(search || {});
        $location.replace();
      }
    };
  }
];
