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

module.exports = [
  '$scope',
  'camAPI',
  'job',
  '$translate',
  'localConf',
  'Uri',
  function($scope, camAPI, job, $translate, localConf, Uri) {
    $scope.loadingState = 'LOADING';

    // prettier-ignore
    $scope.headColumns = [
            {class: 'state', request: 'suspended', sortable: false, content: $translate.instant('PLGN_HIST_STATE')},
            {class: 'message', request: 'failureLog', sortable: false, content: $translate.instant('PLGN_HIST_MESSAGE')},
            {class: 'timestamp', request: 'timestamp', sortable: true, content: $translate.instant('PLGN_HIST_TIMESTAMPE')},
            {class: 'job uuid', request: 'jobId', sortable: true, content: $translate.instant('PLGN_HIST_JOB_ID')},
            {class: 'type', request: 'jobDefinitionType', sortable: false, content: $translate.instant('PLGN_HIST_TYPE')},
            {class: 'configuration', request: 'jobDefinitionConfiguration', sortable: false, content: $translate.instant('PLGN_HIST_CONFIGURATION')},
            {class: 'retries', request: 'jobRetries', sortable: true, content: $translate.instant('PLGN_HIST_RETRIES')},
            {class: 'hostname', request: 'hostname', sortable: true, content: $translate.instant('PLGN_HIST_HOSTNAME')},
            {class: 'priority', request: 'jobPriority', sortable: true, content: $translate.instant('PLGN_HIST_PRIORITY')}
          ];

    $scope.getHistoricJobLogStacktraceUrl = function(log) {
      return Uri.appUri(
        'engine://engine/:engine/history/job-log/' + log.id + '/stacktrace'
      );
    };

    // Default sorting, newest failure on top
    $scope.sortObj = {sortBy: 'timestamp', sortOrder: 'desc'};

    $scope.onSortChange = function(sortObj) {
      $scope.sortObj = sortObj || $scope.sortObj;
      updateView();
    };

    var pages = ($scope.pages = {total: 0, current: 1, size: 10});

    $scope.getState = function(log) {
      if (log.creationLog) {
        return 'Created';
      }
      if (log.failureLog) {
        return 'Failed';
      }
      if (log.deletionLog) {
        return 'Deleted';
      }
      if (log.successLog) {
        return 'Successful';
      }
    };

    var updateView = ($scope.updateView = function() {
      $scope.loadingState = 'LOADING';

      camAPI
        .resource('history')
        .jobLogList({
          jobId: job.id,
          maxResults: pages.size,
          firstResult: (pages.current - 1) * pages.size,
          ...$scope.sortObj
        })
        .then(res => {
          $scope.logs = res;
          $scope.loadingState = res.length ? 'LOADED' : 'EMPTY';
        })
        .catch(() => {
          $scope.loadingState = 'ERROR';
        });
    });

    camAPI
      .resource('history')
      .jobLogCount({jobId: job.id})
      .then(res => {
        pages.total = res.count;
      });

    updateView();
  }
];
