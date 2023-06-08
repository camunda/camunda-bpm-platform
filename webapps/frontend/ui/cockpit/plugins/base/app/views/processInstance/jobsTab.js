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
var moment = require('camunda-commons-ui/vendor/moment');

var jobsTemplate = require('./jobs-tab.html?raw');
var jobRescheduleTemplate = require('./jobs-reschedule-modal.html?raw');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
    id: 'jobs-tab',
    label: 'PLUGIN_JOBS_LABEL',
    template: jobsTemplate,
    priority: 0,
    controller: [
      '$scope',
      'camAPI',
      'Notifications',
      '$translate',
      '$uibModal',
      'localConf',
      '$q',
      function(
        $scope,
        camAPI,
        Notifications,
        $translate,
        $modal,
        localConf,
        $q
      ) {
        var jobProvider = camAPI.resource('job');
        var jobDefinitionProvider = camAPI.resource('job-definition');
        var processInstance = $scope.processInstance;

        $scope.pages = {size: 50, total: 0, current: 1};
        $scope.bpmnElements = [];

        var sorting = ($scope.sorting = loadLocal({
          sortBy: 'jobId',
          sortOrder: 'desc'
        }));

        // prettier-ignore
        $scope.headColumns = [
          { class: 'id', request: 'jobId', sortable: true, content: $translate.instant('PLUGIN_JOBS_ID') },
          { class: 'dueDate', request: 'jobDueDate', sortable: true, content: $translate.instant('PLUGIN_JOBS_DATE') },
          { class: 'createTime', request: 'createTime', sortable: false, content: $translate.instant('PLUGIN_JOBS_CREATION_DATE') },
          { class: 'retries', request: 'jobRetries', sortable: true, content: $translate.instant('PLUGIN_JOBS_RETRIES') },
          { class: 'activityName', request: 'activityName', sortable: false, content: $translate.instant('PLUGIN_JOBS_ACTIVITY') },
          { class: 'failedActivityId', request: 'failedActivityId', sortable: false, content: $translate.instant('PLGN_HIST_FAILING_ACTIVITY') },
          { class: 'action', request: '', sortable: false, content: $translate.instant('PLUGIN_JOBS_ACTION') }
        ];

        function loadLocal(defaultValue) {
          return localConf.get('sortPIJobsTab', defaultValue);
        }

        $scope.onSortChange = function(sortObj) {
          sorting = sortObj;
          saveLocal(sorting);
          updateView(sorting);
        };

        function saveLocal(sorting) {
          localConf.set('sortPIJobsTab', sorting);
        }

        $scope.onPaginationChange = function(pages) {
          $scope.pages.current = pages.current;
          updateView(sorting);
        };

        var updateView = function(sorting) {
          var page = $scope.pages.current,
            count = $scope.pages.size,
            firstResult = (page - 1) * count;

          var queryParams = {
            processInstanceId: processInstance.id,
            firstResult: firstResult,
            maxResults: count,
            sorting: [
              {
                sortBy: sorting.sortBy,
                sortOrder: sorting.sortOrder
              }
            ]
          };

          //get 'count' of jobs
          jobProvider.count(queryParams, function(error, response) {
            $scope.pages.total = response;
          });

          //get current page and display it
          $scope.loadingState = 'LOADING';
          jobProvider.list(queryParams, jobCallback);
        };

        var jobCallback = function(err, res) {
          if (err) {
            Notifications.addError({
              status: $translate.instant('PLUGIN_JOBS'),
              message: $translate.instant('PLUGIN_JOBS_LOADING_ERROR')
            });
          } else {
            //Load Job definitions
            $scope.jobs = res;

            var jobDefinitionIds = $scope.jobs.reduce(function(
              accumulate,
              current
            ) {
              // IE11 - Sets return undefined after .add()
              if (current.jobDefinitionId) {
                accumulate.add(current.jobDefinitionId);
              }
              return accumulate;
            },
            new Set());

            var promises = [];
            jobDefinitionIds.forEach(function(id) {
              promises.push(jobDefinitionProvider.get(id));
            });

            $q.all(promises).then(function(res) {
              var processDefinitions = res;
              $scope.jobs = $scope.jobs.map(function(job) {
                var definition = processDefinitions.filter(function(
                  definition
                ) {
                  return definition.id === job.jobDefinitionId;
                })[0];

                if (definition) {
                  job.activityId = definition.activityId;
                }
                return job;
              });

              updateActivityNames();
              $scope.loadingState = $scope.jobs.length ? 'LOADED' : 'EMPTY';
            });
          }
        };

        var recalculateDate = function(job, useCreationDate) {
          jobProvider.recalculateDuedate(
            {id: job.id, creationDateBased: useCreationDate},
            function(err) {
              if (err) {
                Notifications.addError({
                  status: $translate.instant('PLUGIN_JOBS_RECALCULATE_ERROR'),
                  message: $translate.instant(
                    'PLUGIN_JOBS_RECALCULATE_ERROR_MESSAGE'
                  ),
                  exclusive: true
                });
              } else {
                Notifications.addMessage({
                  status: $translate.instant('PLUGIN_JOBS_RECALCULATE_SUCCESS'),
                  message: $translate.instant(
                    'PLUGIN_JOBS_RECALCULATE_SUCCESS_MESSAGE'
                  ),
                  exclusive: true
                });
                updateJob(job);
              }
            }
          );
        };

        var setDuedate = function(job, date, cascade) {
          jobProvider.setDuedate(
            {id: job.id, duedate: date, cascade: cascade},
            function(err) {
              if (err) {
                Notifications.addError({
                  status: $translate.instant('PLUGIN_JOBS_RECALCULATE_ERROR'),
                  message: $translate.instant(
                    'PLUGIN_JOBS_SET_DUEDATE_ERROR_MESSAGE'
                  ),
                  exclusive: true
                });
              } else {
                Notifications.addMessage({
                  status: $translate.instant('PLUGIN_JOBS_RECALCULATE_SUCCESS'),
                  message: $translate.instant(
                    'PLUGIN_JOBS_SET_DUEDATE_SUCCESS_MESSAGE'
                  ),
                  exclusive: true
                });
                updateJob(job);
              }
            }
          );
        };

        var updateJob = function(job) {
          jobProvider.get(job.id, function(err, res) {
            if (res) {
              //use cached activityName to avoid unnecessary requests
              res.activityName = job.activityName;
              res.activityId = job.activityId;
              $scope.jobs[$scope.jobs.indexOf(job)] = res;
            }
          });
        };

        $scope.openRecalculationWindow = function(job) {
          $modal
            .open({
              controller: [
                '$scope',
                '$filter',
                function($scope, $filter) {
                  var dateFilter = $filter('date'),
                    dateFormat = "yyyy-MM-dd'T'HH:mm:ss";

                  $scope.cascade = true;
                  $scope.date = dateFilter(Date.now(), dateFormat);
                  $scope.submit = function() {
                    switch ($scope.recalculationType) {
                      case 'specific':
                        setDuedate(
                          job,
                          moment($scope.date, moment.ISO_8601).format(
                            'YYYY-MM-DDTHH:mm:ss.SSSZZ'
                          ),
                          $scope.cascade
                        );
                        break;
                      case 'now':
                        recalculateDate(job, false);
                        break;
                      case 'creation':
                        recalculateDate(job, true);
                        break;
                    }
                    $scope.status = 'DONE';
                  };

                  $scope.isValid = function() {
                    return $scope.recalculationType === 'specific'
                      ? this.rescheduleJobDuedateForm.$valid
                      : true;
                  };
                }
              ],
              template: jobRescheduleTemplate
            })
            .result.catch(angular.noop);
        };

        $scope.toggleSuspension = function(job) {
          jobProvider.suspended(
            {id: job.id, suspended: !job.suspended},
            function(err) {
              if (err) {
                Notifications.addError({
                  status: $translate.instant('PLUGIN_JOBS_ERROR'),
                  message: $translate.instant('PLUGIN_JOBS_SUSPEND_FAILURE'),
                  exclusive: false,
                  duration: 5000
                });
              } else {
                job.suspended = !job.suspended;
                Notifications.addMessage({
                  status: $translate.instant('PLUGIN_JOBS_SUCCESS'),
                  message: $translate.instant('PLUGIN_JOBS_SUSPEND_SUCCESS'),
                  exclusive: false,
                  duration: 5000
                });
              }
            }
          );
        };

        var updateActivityNames = function() {
          //map job names to bpmn element name
          angular.forEach($scope.jobs, function(job) {
            var activityId = job.activityId,
              bpmnElement = $scope.bpmnElements[activityId];

            job.activityName =
              (bpmnElement && (bpmnElement.name || bpmnElement.id)) ||
              activityId;

            var failedActivityId = job.failedActivityId;
            var failedElement = $scope.bpmnElements[failedActivityId];
            job.failedActivityName =
              (failedElement && (failedElement.name || failedElement.id)) ||
              failedActivityId;
          });
        };

        $scope.processData.observe(['bpmnElements'], function(bpmnElements) {
          $scope.bpmnElements = bpmnElements;
          updateActivityNames();
        });

        $scope.loadingState = 'LOADING';
      }
    ]
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
