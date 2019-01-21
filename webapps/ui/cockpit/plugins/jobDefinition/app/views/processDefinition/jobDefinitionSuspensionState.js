'use strict';

var angular = require('angular');

module.exports = [
  '$scope', '$http', '$filter', 'Uri', 'Notifications', '$uibModalInstance', 'jobDefinition', '$translate', 'fixDate',
  function($scope,   $http,   $filter,   Uri,   Notifications,   $modalInstance,   jobDefinition, $translate, fixDate) {

    var BEFORE_UPDATE = 'BEFORE_UPDATE',
        PERFORM_UPDATE = 'PERFORM_UDPATE',
        UPDATE_SUCCESS = 'SUCCESS',
        UPDATE_FAILED = 'FAIL';

    var dateFilter = $filter('date'),
        dateFormat = 'yyyy-MM-dd\'T\'HH:mm:ss';

    $scope.jobDefinition = jobDefinition;

    $scope.status = BEFORE_UPDATE;

    $scope.data = {
      includeJobs : true,
      executeImmediately : true,
      executionDate : dateFilter(Date.now(), dateFormat)
    };

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.updateSuspensionState = function() {
      $scope.status = PERFORM_UPDATE;

      var data = {};
      data.suspended = !jobDefinition.suspended;
      data.includeJobs = $scope.data.includeJobs;
      data.executionDate = !$scope.data.executeImmediately ? fixDate($scope.data.executionDate) : null;

      $http.put(Uri.appUri('engine://engine/:engine/job-definition/' + jobDefinition.id + '/suspended/'), data).then(function() {
        $scope.status = UPDATE_SUCCESS;

        if ($scope.data.executeImmediately) {
          Notifications.addMessage({'status': $translate.instant('PLUGIN_JOBDEFINITION_STATE_STATUS'), 'message': $translate.instant('PLUGIN_JOBDEFINITION_STATE_MESSAGES_1'), 'exclusive': true });
        } else {
          Notifications.addMessage({'status': $translate.instant('PLUGIN_JOBDEFINITION_STATE_STATUS'), 'message': $translate.instant('PLUGIN_JOBDEFINITION_STATE_MESSAGES_2'), 'exclusive': true });
        }

      }).catch(function(data) {
        $scope.status = UPDATE_FAILED;

        if ($scope.data.executeImmediately) {
          Notifications.addError({'status': $translate.instant('PLUGIN_JOBDEFINITION_STATE_STATUS'), 'message': $translate.instant('PLUGIN_JOBDEFINITION_STATE_ERR_1', { message: data.message }), 'exclusive': true });
        } else {
          Notifications.addError({'status': $translate.instant('PLUGIN_JOBDEFINITION_STATE_STATUS'), 'message': $translate.instant('PLUGIN_JOBDEFINITION_STATE_ERR_2', { message: data.message }), 'exclusive': true });
        }
      });
    };

    $scope.isValid = function() {
      var formScope = angular.element('[name="updateSuspensionStateForm"]').scope();
      return (formScope && formScope.updateSuspensionStateForm) ? formScope.updateSuspensionStateForm.$valid : false;
    };

    $scope.close = function(status) {
      var response = {};

      response.status = status;
      response.suspended = !jobDefinition.suspended;
      response.executeImmediately = $scope.data.executeImmediately;
      response.executionDate = $scope.data.executionDate;

      $modalInstance.close(response);

    };

  }];
