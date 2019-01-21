'use strict';

var angular = require('angular');

module.exports = [
  '$scope', '$http', '$filter', 'Uri', 'Notifications', '$uibModalInstance', 'processDefinition','fixDate', '$translate',
  function($scope,   $http,   $filter,   Uri,   Notifications,   $modalInstance,   processDefinition, fixDate, $translate) {

    var BEFORE_UPDATE = 'BEFORE_UPDATE',
        PERFORM_UPDATE = 'PERFORM_UDPATE',
        UPDATE_SUCCESS = 'SUCCESS',
        UPDATE_FAILED = 'FAIL';

    var dateFilter = $filter('date'),
        dateFormat = 'yyyy-MM-dd\'T\'HH:mm:ss';

    $scope.processDefinition = processDefinition;

    $scope.status = BEFORE_UPDATE;

    $scope.data = {
      includeInstances : true,
      executeImmediately : true,
      executionDate : dateFilter(Date.now(), dateFormat)
    };

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.updateSuspensionState = function() {
      $scope.status = PERFORM_UPDATE;

      var data = {};

      data.suspended = !processDefinition.suspended;
      data.includeProcessInstances = $scope.data.includeInstances;
      data.executionDate = !$scope.data.executeImmediately ? fixDate($scope.data.executionDate) : null;

      $http
      .put(Uri.appUri('engine://engine/:engine/process-definition/' + processDefinition.id + '/suspended/'), data)
      .then(function() {
        $scope.status = UPDATE_SUCCESS;

        if ($scope.data.executeImmediately) {
          Notifications.addMessage({
            status: $translate.instant('PLUGIN_UPDATE_SUSPENSION_STATE_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_UPDATE_SUSPENSION_STATE_MESSAGE_1'),
            exclusive: true
          });
        } else {
          Notifications.addMessage({
            status: $translate.instant('PLUGIN_UPDATE_SUSPENSION_STATE_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_UPDATE_SUSPENSION_STATE_MESSAGE_2'),
            exclusive: true
          });
        }

      }).catch(function(response) {
        $scope.status = UPDATE_FAILED;
        var errorMessage;
        if($scope.data.executeImmediately) {
          errorMessage = $translate.instant('PLUGIN_UPDATE_SUSPENSION_STATE_MESSAGE_3', { message: response.message });
        } else {
          errorMessage = $translate.instant('PLUGIN_UPDATE_SUSPENSION_STATE_MESSAGE_4', { message: response.message });
        }
        Notifications.addError({
          status: $translate.instant('PLUGIN_UPDATE_SUSPENSION_STATE_STATUS_FINISHED'),
          message: errorMessage,
          exclusive: true
        });

      });
    };

    $scope.isValid = function() {
      var formScope = angular.element('[name="updateSuspensionStateForm"]').scope();
      return (formScope && formScope.updateSuspensionStateForm) ? formScope.updateSuspensionStateForm.$valid : false;
    };

    $scope.close = function(status) {
      var response = {};

      response.status = status;
      response.suspended = !processDefinition.suspended;
      response.executeImmediately = $scope.data.executeImmediately;
      response.executionDate = $scope.data.executionDate;

      $modalInstance.close(response);
    };

  }];
