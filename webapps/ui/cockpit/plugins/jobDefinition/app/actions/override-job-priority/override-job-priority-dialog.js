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
  '$q',
  'Notifications',
  'JobDefinitionResource',
  '$uibModalInstance',
  'jobDefinition',
  '$translate',
  function(
    $scope,
    $q,
    Notifications,
    JobDefinitionResource,
    $modalInstance,
    jobDefinition,
    $translate
  ) {
    var SUCCESS = 'SUCCESS',
      FAILED = 'FAIL';

    $scope.status;
    $scope.setJobPriority = true;

    var data = ($scope.data = {
      priority: jobDefinition.overridingJobPriority,
      includeJobs: false
    });

    $scope.$on('$routeChangeStart', function() {
      var response = {};
      response.status = $scope.status;
      $modalInstance.close(response);
    });

    $scope.hasOverridingJobPriority = function() {
      return (
        jobDefinition.overridingJobPriority !== null &&
        jobDefinition.overridingJobPriority !== undefined
      );
    };

    $scope.submit = function() {
      var setJobPriority = $scope.setJobPriority;
      if (!setJobPriority) {
        data = {};
      }

      JobDefinitionResource.setJobPriority(
        {id: jobDefinition.id},
        data,

        function() {
          $scope.status = SUCCESS;
          if (setJobPriority) {
            Notifications.addMessage({
              status: $translate.instant(
                'PLUGIN_JOBDEFINITION_ACTION_STATUS_FINISHED'
              ),
              message: $translate.instant(
                'PLUGIN_JOBDEFINITION_ACTION_DIALOG_MSN_1'
              ),
              exclusive: true
            });
          } else {
            Notifications.addMessage({
              status: $translate.instant(
                'PLUGIN_JOBDEFINITION_ACTION_STATUS_FINISHED'
              ),
              message: $translate.instant(
                'PLUGIN_JOBDEFINITION_ACTION_DIALOG_MSN_2'
              ),
              exclusive: true
            });
          }
        },

        function(error) {
          $scope.status = FAILED;
          if (setJobPriority) {
            Notifications.addError({
              status: $translate.instant(
                'PLUGIN_JOBDEFINITION_ACTION_STATUS_FINISHED'
              ),
              message: $translate.instant(
                'PLUGIN_JOBDEFINITION_ACTION_DIALOG_ERR_1',
                {message: error.data.message}
              ),
              exclusive: true
            });
          } else {
            Notifications.addError({
              status: $translate.instant(
                'PLUGIN_JOBDEFINITION_ACTION_STATUS_FINISHED'
              ),
              message: $translate.instant(
                'PLUGIN_JOBDEFINITION_ACTION_DIALOG_ERR_2',
                {message: error.data.message}
              ),
              exclusive: true
            });
          }
        }
      );
    };

    $scope.isValid = function() {
      var formScope = angular
        .element('[name="overrideJobPriorityForm"]')
        .scope();
      return (
        !$scope.setJobPriority ||
        (formScope && formScope.overrideJobPriorityForm
          ? formScope.overrideJobPriorityForm.$valid
          : false)
      );
    };

    $scope.close = function(status) {
      var response = {};
      response.status = status;
      $modalInstance.close(response);
    };
  }
];
