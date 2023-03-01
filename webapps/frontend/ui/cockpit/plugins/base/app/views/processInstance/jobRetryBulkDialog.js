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
var moment = require('moment');

module.exports = [
  '$scope',
  '$q',
  'Notifications',
  'JobResource',
  '$uibModalInstance',
  'processData',
  'processInstance',
  '$translate',
  'fixDate',
  function(
    $scope,
    $q,
    Notifications,
    JobResource,
    $modalInstance,
    processData,
    processInstance,
    $translate,
    fixDate
  ) {
    $scope.radio = {value: 'preserveDueDate'};
    $scope.dueDate = moment().format('YYYY-MM-DDTHH:mm:00');

    const checkDateFormat = ($scope.checkDateFormat = () =>
      /(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:.(\d\d\d)| )?$/.test(
        $scope.dueDate
      ));

    $scope.changeDueDate = dueDate => {
      $scope.dueDate = dueDate;
    };

    $scope.checkRetryDisabled = () =>
      !$scope.failedJobs ||
      !$scope.failedJobs.length ||
      !$scope.selectedFailedJobIds.length ||
      ($scope.radio.value === 'dueDate' &&
        (!$scope.dueDate || ($scope.dueDate && !checkDateFormat())));

    var jobRetriesData = processData.newChild($scope);

    var jobPages = ($scope.jobPages = {size: 5, total: 0});
    var summarizePages = ($scope.summarizePages = {size: 5, total: 0});

    var jobIdToFailedJobMap = {};
    var selectedFailedJobIds = ($scope.selectedFailedJobIds = []);

    var finishedWithFailures = false;

    $scope.form = {
      allJobsSelected: false
    };

    var FINISHED = 'finished',
      PERFORM = 'performing',
      SUCCESS = 'successful',
      FAILED = 'failed';

    let executionIdToInstanceMap = null;
    try {
      jobRetriesData.observe('executionIdToInstanceMap', function(
        executionMap
      ) {
        executionIdToInstanceMap = executionMap;
        $scope.runtimeView = true;
      });
    } catch (ignored) {
      $scope.runtimeView = false;
    }

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.$watch('jobPages.current', function(newValue, oldValue) {
      if (!newValue) {
        jobPages.current = 1;
        return;
      }

      if (newValue === oldValue) {
        return;
      }

      jobPages.current = newValue;
      updateJobTable(newValue);
    });

    function updateJobTable(page) {
      $scope.failedJobs = null;
      $scope.form.allJobsSelected = false;
      $scope.loadingState = 'LOADING';

      var count = jobPages.size;
      var firstResult = (page - 1) * count;

      JobResource.count({
        processInstanceId: processInstance.id,
        withException: true,
        noRetriesLeft: true
      })
        .$promise.then(function(data) {
          jobPages.total = data.count;

          if (!jobPages.total) {
            $scope.loadingState = 'EMPTY';
            return;
          }

          JobResource.query(
            {
              firstResult: firstResult,
              maxResults: count
            },
            {
              processInstanceId: processInstance.id,
              withException: true,
              noRetriesLeft: true
            }
          )
            .$promise.then(function(response) {
              for (var i = 0, job; (job = response[i]); i++) {
                jobIdToFailedJobMap[job.id] = job;
                if ($scope.runtimeView) {
                  job.instance = executionIdToInstanceMap[job.executionId];
                }
                job.selected = selectedFailedJobIds.indexOf(job.id) > -1;
              }

              $scope.failedJobs = response;
              $scope.loadingState = 'LOADED';

              if (jobPages.total <= count) {
                $scope.form.allJobsSelected = true;
                $scope.selectAllJobs(true);
              }
            })
            .catch(angular.noop);
        })
        .catch(angular.noop);
    }

    $scope.$watch('summarizePages.current', function(newValue) {
      if (!newValue) {
        return;
      }

      updateSummarizeTable(newValue);
    });

    function updateSummarizeTable(page) {
      var count = summarizePages.size;
      var firstResult = (page - 1) * count;

      var showJobsRetried = ($scope.showJobsRetried = []);

      for (var i = 0; i < count; i++) {
        var jobId = selectedFailedJobIds[i + firstResult];
        var job = jobIdToFailedJobMap[jobId];
        if (job) {
          showJobsRetried.push(job);
        }
      }
    }

    $scope.selectAllJobs = function(allJobsSelected) {
      angular.forEach($scope.failedJobs, function(job) {
        job.selected = allJobsSelected;
        selectFailedJob(job);
      });
    };

    var selectFailedJob = ($scope.selectFailedJob = function(failedJob) {
      var index = selectedFailedJobIds.indexOf(failedJob.id);

      if (failedJob.selected === true) {
        if (index === -1) {
          selectedFailedJobIds.push(failedJob.id);
        }
        return;
      }

      if (failedJob.selected === false) {
        selectedFailedJobIds.splice(index, 1);
        if ($scope.form.allJobsSelected === true) {
          $scope.form.allJobsSelected = false;
        }
      }
    });

    $scope.retryFailedJobs = function(selectedFailedJobIds) {
      $scope.status = PERFORM;

      summarizePages.total = selectedFailedJobIds.length;
      summarizePages.current = 1;

      doRetry(selectedFailedJobIds)
        .then(function() {
          if (!finishedWithFailures) {
            Notifications.addMessage({
              status: $translate.instant('PLUGIN_JOB_RETRY_STATUS_FINISHED'),
              message: $translate.instant('PLUGIN_JOB_RETRY_MESSAGE_2'),
              exclusive: true
            });
          } else {
            Notifications.addError({
              status: $translate.instant('PLUGIN_JOB_RETRY_STATUS_FINISHED'),
              message: $translate.instant('PLUGIN_JOB_RETRY_ERROR_2'),
              exclusive: true
            });
          }

          $scope.status = FINISHED;
        })
        .catch(angular.noop);
    };

    function doRetry(selectedFailedJobIds) {
      var deferred = $q.defer();

      var count = selectedFailedJobIds.length;

      function retryJob(job) {
        job.status = PERFORM;

        let payload = {
          retries: 1
        };

        if ($scope.radio.value === 'dueDate') {
          payload = {...payload, dueDate: fixDate($scope.dueDate)};
        }

        JobResource.setRetries(
          {
            id: job.id
          },
          payload,
          function() {
            job.status = SUCCESS;

            // we want to show a summarize, when all requests
            // responded, that's why we uses a counter
            count = count - 1;
            if (count === 0) {
              deferred.resolve();
            }
          },
          function(error) {
            finishedWithFailures = true;

            job.status = FAILED;
            job.retryError = error;

            // we want to show a summarize, when all requests
            // responded, that's why we uses a counter
            count = count - 1;
            if (count === 0) {
              deferred.resolve();
            }
          }
        );
      }

      for (var i = 0, jobId; (jobId = selectedFailedJobIds[i]); i++) {
        var job = jobIdToFailedJobMap[jobId];
        retryJob(job);
      }

      return deferred.promise;
    }

    $scope.close = function(status) {
      $modalInstance.close(status);
    };
  }
];
