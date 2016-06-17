'use strict';

var angular = require('angular');

module.exports = [
  '$scope', '$http', '$filter', 'Uri', 'Notifications', '$modalInstance', 'jobDefinition',
  function($scope,   $http,   $filter,   Uri,   Notifications,   $modalInstance,   jobDefinition) {

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
      data.executionDate = !$scope.data.executeImmediately ? $scope.data.executionDate : null;

      $http.put(Uri.appUri('engine://engine/:engine/job-definition/' + jobDefinition.id + '/suspended/'), data).success(function() {
        $scope.status = UPDATE_SUCCESS;

        if ($scope.data.executeImmediately) {
          Notifications.addMessage({'status': 'Finished', 'message': 'Updated the suspension state of the job definition.', 'exclusive': true });
        } else {
          Notifications.addMessage({'status': 'Finished', 'message': 'The update of the suspension state of the job definition has been scheduled.', 'exclusive': true });
        }

      }).error(function(data) {
        $scope.status = UPDATE_FAILED;

        if ($scope.data.executeImmediately) {
          Notifications.addError({'status': 'Finished', 'message': 'Could not update the suspension state of the job definition: ' + data.message, 'exclusive': true });
        } else {
          Notifications.addError({'status': 'Finished', 'message': 'The update of the suspension state of the job definition could not be scheduled: ' + data.message, 'exclusive': true });
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
