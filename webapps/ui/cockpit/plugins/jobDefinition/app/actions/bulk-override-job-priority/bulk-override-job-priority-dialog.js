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
  '$timeout',
  '$translate',
  'processData',
  'camAPI',
  function(
    $scope,
    $q,
    Notifications,
    JobDefinitionResource,
    $modalInstance,
    $timeout,
    $translate,
    processData,
    camAPI
  ) {
    var jobDefinitions = [];

    processData.observe([
      'processDefinition',
      'bpmnElements',
      function(processDefinition, bpmnElements) {
        // Load Job Definitions
        function fetchDefinitions(firstResult) {
          camAPI
            .resource('job-definition')
            .list({
              processDefinitionId: processDefinition.id,
              firstResult: firstResult,
              maxResults: 2000
            })
            .then(function(res) {
              jobDefinitions = jobDefinitions.concat(
                res.map(function(jobDef) {
                  jobDef.activityName =
                    bpmnElements[jobDef.activityId] &&
                    bpmnElements[jobDef.activityId].name;
                  return jobDef;
                })
              );
              summarizePages.total = jobDefinitions.length;

              if (!jobDefinitions.length) {
                $scope.hasNoJobDefinitions = true;

                $modalInstance.opened
                  .then(
                    $timeout(function() {
                      Notifications.addError({
                        status: 'Error',
                        message:
                          'This process definition has no job definitions associated with. The job priority cannot be overridden.',
                        exclusive: true
                      });
                    }, 0)
                  )
                  .catch(angular.noop);
                return;
              }

              updateSummarizeTable(1);

              if (res.length === 2000) {
                fetchDefinitions(firstResult + 2000);
              }
            });
        }
        fetchDefinitions(0);
      }
    ]);

    $scope.status;
    var FINISHED = 'FINISHED',
      PERFORM = 'PERFORMING',
      SUCCESS = 'SUCCESS',
      FAILED = 'FAILED';

    var finishedWithFailures = false;

    var summarizePages = ($scope.summarizePages = {
      size: 5,
      total: 0,
      current: 1
    });

    var data = ($scope.data = {
      priority: null,
      includeJobs: false
    });

    $scope.setJobPriority = true;

    $scope.$on('$routeChangeStart', function() {
      var response = {};
      response.status = $scope.status;
      $modalInstance.close(response);
    });

    $scope.$watch('summarizePages.current', function(newValue) {
      if (!newValue) {
        return;
      }

      updateSummarizeTable(newValue);
    });

    function updateSummarizeTable(page) {
      var count = summarizePages.size;
      var firstResult = (page - 1) * count;

      var showJobDefinitions = ($scope.showJobDefinitions = []);

      for (var i = 0; i < count; i++) {
        var jobDefinition = jobDefinitions[i + firstResult];
        if (jobDefinition) {
          showJobDefinitions.push(jobDefinition);
        }
      }
    }

    $scope.submit = function() {
      var setJobPriority = $scope.setJobPriority;
      if (!setJobPriority) {
        data = {};
      }

      overrideJobPriority(jobDefinitions);
    };

    function overrideJobPriority(jobDefinitions) {
      $scope.status = PERFORM;

      doOverride(jobDefinitions)
        .then(function() {
          if (!finishedWithFailures) {
            if ($scope.setJobPriority) {
              Notifications.addMessage({
                status: $translate.instant('BULK_OVERRIDE_STATUS_FINISHED'),
                message: $translate.instant('BULK_OVERRIDE_MESSAGE'),
                exclusive: true
              });
            } else {
              Notifications.addMessage({
                status: $translate.instant('BULK_OVERRIDE_STATUS_FINISHED'),
                message: $translate.instant('BULK_OVERRIDE_CLEARING_MESSAGE'),
                exclusive: true
              });
            }
          } else {
            if ($scope.setJobPriority) {
              Notifications.addError({
                status: $translate.instant('BULK_OVERRIDE_STATUS_FINISHED'),
                message: $translate.instant('BULK_OVERRIDE_ERROR_1'),
                exclusive: true
              });
            } else {
              Notifications.addError({
                status: $translate.instant('BULK_OVERRIDE_STATUS_FINISHED'),
                message: $translate.instant('BULK_OVERRIDE_ERROR_2'),
                exclusive: true
              });
            }
          }

          $scope.status = FINISHED;
        })
        .catch(angular.noop);
    }

    function doOverride(jobDefinitions) {
      var deferred = $q.defer();

      var count = jobDefinitions.length;

      function setJobPriority(jobDefinition) {
        jobDefinition.status = PERFORM;
        JobDefinitionResource.setJobPriority(
          {
            id: jobDefinition.id
          },
          data,
          function() {
            jobDefinition.status = SUCCESS;

            // we want to show a summarize, when all requests
            // responded, that's why we uses a counter
            count = count - 1;
            if (count === 0) {
              deferred.resolve();
            }
          },
          function(error) {
            finishedWithFailures = true;

            jobDefinition.status = FAILED;
            jobDefinition.error = error;

            // we want to show a summarize, when all requests
            // responded, that's why we uses a counter
            count = count - 1;
            if (count === 0) {
              deferred.resolve();
            }
          }
        );
      }

      for (var i = 0, jobDefinition; (jobDefinition = jobDefinitions[i]); i++) {
        setJobPriority(jobDefinition);
      }

      return deferred.promise;
    }

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
