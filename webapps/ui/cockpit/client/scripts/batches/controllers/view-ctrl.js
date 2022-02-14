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

var Ctrl = require('../components/batch');
var events = require('../components/events');

var fs = require('fs');

var deleteModalTemplate = fs.readFileSync(
  __dirname + '/../templates/delete-modal.html',
  'utf8'
);
var deleteModalCtrl = require('./modal-ctrl');

var jobLogModalCtrl = require('./job-log-ctrl');
var jobLogModalTemplate = fs.readFileSync(
  __dirname + '/../templates/job-log.html',
  'utf8'
);

module.exports = [
  '$scope',
  'page',
  'camAPI',
  '$location',
  '$uibModal',
  '$translate',
  'Notifications',
  'localConf',
  'configuration',
  function(
    $scope,
    page,
    camAPI,
    $location,
    $modal,
    $translate,
    Notifications,
    localConf,
    configuration
  ) {
    $scope.runtimeHeadColumns = [
      {
        class: 'id',
        request: 'batchId',
        sortable: true,
        content: $translate.instant('BATCHES_PROGRESS_ID')
      },
      {
        class: 'type',
        request: '',
        sortable: false,
        content: $translate.instant('BATCHES_PROGRESS_TYPE')
      },
      {
        class: 'failed',
        request: '',
        sortable: false,
        content: $translate.instant('BATCHES_PROGRESS_FAIL_JOBS')
      },
      {
        class: '',
        request: '',
        sortable: false,
        content: $translate.instant('BATCHES_PROGRESS_PROGRESS')
      }
    ];

    $scope.historyHeadColumns = [
      {
        class: 'id',
        request: 'batchId',
        sortable: true,
        content: $translate.instant('BATCHES_PROGRESS_ID')
      },
      {
        class: 'type',
        request: '',
        sortable: false,
        content: $translate.instant('BATCHES_PROGRESS_TYPE')
      },
      {
        class: 'start-time',
        request: 'startTime',
        sortable: true,
        content: $translate.instant('BATCHES_PROGRESS_START_TIME')
      },
      {
        class: 'end-time',
        request: 'endTime',
        sortable: true,
        content: $translate.instant('BATCHES_PROGRESS_END_TIME')
      }
    ];

    $scope.jobHeadColumns = [
      {
        class: 'id',
        request: 'jobId',
        sortable: true,
        content: $translate.instant('BATCHES_PROGRESS_ID')
      },
      {
        class: 'type',
        request: '',
        sortable: false,
        content: $translate.instant('BATCHES_PROGRESS_EXCEPTION')
      },
      {
        class: 'action',
        request: '',
        sortable: false,
        content: $translate.instant('BATCHES_PROGRESS_ACTIONS')
      }
    ];

    $scope.$on('$destroy', function() {
      events.removeAllListeners();
      $scope.ctrl.stopLoadingPeriodically();
    });

    $scope.$watch(
      function() {
        return $location.search() || {};
      },
      function(newValue) {
        if (newValue.details && newValue.type) {
          $scope.ctrl.loadDetails(newValue.details, newValue.type);
        }
      }
    );

    events.on('details:switchToHistory', function() {
      $location.search('type', 'history');
    });

    events.on('deleteModal:open', function(deleteModal) {
      var modal = (deleteModal.instance = $modal.open({
        template: deleteModalTemplate,
        controller: deleteModalCtrl
      }));

      modal.result.catch(function() {});
    });

    events.on('batch:delete:failed', function(err) {
      Notifications.addError({
        status: $translate.instant('BATCHES_DELETE_BATCH_STATUS'),
        message: $translate.instant('BATCHES_DELETE_BATCH_FAILED', {
          message: err.message
        }),
        exclusive: true
      });
    });

    events.on('job:delete:failed', function(err) {
      Notifications.addError({
        status: $translate.instant('BATCHES_DELETE_JOB_STATUS'),
        message: $translate.instant('BATCHES_DELETE_JOB_FAILED', {
          message: err.message
        }),
        exclusive: true
      });
    });

    events.on('job:retry:failed', function(err) {
      Notifications.addError({
        status: $translate.instant('BATCHES_RETRY_JOB_STATUS'),
        message: $translate.instant('BATCHES_RETRY_JOB_FAILED', {
          message: err.message
        }),
        exclusive: true
      });
    });

    require('../components/breadcrumbs')(page, $scope.$root, $translate);

    $scope.ctrl = new Ctrl(camAPI, localConf, configuration);
    $scope.ctrl.loadPeriodically(5000);

    $scope.openLog = function(job) {
      $modal.open({
        controller: jobLogModalCtrl,
        template: jobLogModalTemplate,
        resolve: {
          job: function() {
            return job;
          }
        },
        size: 'lg'
      });
    };
  }
];
