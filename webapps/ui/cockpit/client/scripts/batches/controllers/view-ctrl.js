'use strict';

var Ctrl = require('../components/batch');
var events = require('../components/events');

var fs = require('fs');

var deleteModalTemplate = fs.readFileSync(__dirname + '/../templates/delete-modal.html', 'utf8');
var deleteModalCtrl = require('./modal-ctrl');

module.exports = [
  '$scope',
  'page',
  'camAPI',
  '$location',
  '$uibModal',
  '$translate',
  'Notifications',
  'localConf',
  function(
  $scope,
  page,
  camAPI,
  $location,
  $modal,
  $translate,
  Notifications,
  localConf
) {

    $scope.runtimeHeadColumns = [
      { class: 'id', request: 'batchId', sortable: true, content: $translate.instant('BATCHES_PROGRESS_ID') },
      { class: 'type', request: '', sortable: false, content: $translate.instant('BATCHES_PROGRESS_TYPE') },
      { class: 'failed', request: '', sortable: false, content: $translate.instant('BATCHES_PROGRESS_FAIL_JOBS') },
      { class: '', request: '', sortable: false, content: $translate.instant('BATCHES_PROGRESS_PROGRESS') }
    ];

    $scope.historyHeadColumns = [
      { class: 'id', request: 'batchId', sortable: true, content: $translate.instant('BATCHES_PROGRESS_ID') },
      { class: 'type', request: '', sortable: false, content: $translate.instant('BATCHES_PROGRESS_TYPE') },
      { class: 'start-time', request: 'startTime', sortable: true, content: $translate.instant('BATCHES_PROGRESS_START_TIME') },
      { class: 'end-time', request: 'endTime', sortable: true, content: $translate.instant('BATCHES_PROGRESS_END_TIME') }
    ];

    $scope.jobHeadColumns = [
      { class: 'id', request: 'jobId', sortable: true, content: $translate.instant('BATCHES_PROGRESS_ID') },
      { class: 'type', request: '', sortable: false, content: $translate.instant('BATCHES_PROGRESS_EXCEPTION') },
      { class: 'action', request: '', sortable: false, content: $translate.instant('BATCHES_PROGRESS_ACTIONS') }
    ];

    $scope.$on('$destroy', function() {
      events.removeAllListeners();
      $scope.ctrl.stopLoadingPeriodically();
    });

    $scope.$watch(function() {
      return ($location.search() || {});
    }, function(newValue) {
      if(newValue.details && newValue.type) {
        $scope.ctrl.loadDetails(newValue.details, newValue.type);
      }
    });

    events.on('details:switchToHistory', function() {
      $location.search('type', 'history');
    });

    events.on('deleteModal:open', function(deleteModal) {
      deleteModal.instance = $modal.open({
        template: deleteModalTemplate,
        controller: deleteModalCtrl
      });
    });

    events.on('batch:delete:failed', function(err) {
      Notifications.addError({
        status: $translate.instant('BATCHES_DELETE_BATCH_STATUS'),
        message: $translate.instant('BATCHES_DELETE_BATCH_FAILED', {message: err.message}),
        exclusive: true
      });
    });

    events.on('job:delete:failed', function(err) {
      Notifications.addError({
        status: $translate.instant('BATCHES_DELETE_JOB_STATUS'),
        message: $translate.instant('BATCHES_DELETE_JOB_FAILED', {message: err.message}),
        exclusive: true
      });
    });

    events.on('job:retry:failed', function(err) {
      Notifications.addError({
        status: $translate.instant('BATCHES_RETRY_JOB_STATUS'),
        message: $translate.instant('BATCHES_RETRY_JOB_FAILED', {message: err.message}),
        exclusive: true
      });
    });

    require('../components/breadcrumbs')(page, $scope.$root, $translate);

    $scope.ctrl = new Ctrl(camAPI, localConf);
    $scope.ctrl.loadPeriodically(5000);
  }];
