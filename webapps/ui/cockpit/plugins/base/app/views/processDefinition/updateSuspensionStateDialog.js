'use strict';

var angular = require('angular');

module.exports = [
  '$scope', '$http', '$filter', 'Uri', 'Notifications', '$modalInstance', 'processDefinition',
  function($scope,   $http,   $filter,   Uri,   Notifications,   $modalInstance,   processDefinition) {

    var BEFORE_UPDATE = 'BEFORE_UPDATE',
        PERFORM_UPDATE = 'PERFORM_UDPATE',
        UPDATE_SUCCESS = 'SUCCESS',
        UPDATE_FAILED = 'FAIL';

    var dateFilter = $filter('date'),
        dateFormat = 'yyyy-MM-dd\'T\'HH:mm:ss',
        timezoneDateFormat = 'yyyy-MM-dd\'T\'HH:mm:ss\'Z';

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
      data.executionDate = !$scope.data.executeImmediately ? dateFilter($scope.data.executionDate, timezoneDateFormat) : null;

      $http
      .put(Uri.appUri('engine://engine/:engine/process-definition/' + processDefinition.id + '/suspended/'), data)
      .success(function() {
        $scope.status = UPDATE_SUCCESS;

        if ($scope.data.executeImmediately) {
          Notifications.addMessage({
            status: 'Finished',
            message: 'Updated the suspension state of the process definition.',
            exclusive: true
          });
        } else {
          Notifications.addMessage({
            status: 'Finished',
            message: 'The update of the suspension state of the process definition has been scheduled.',
            exclusive: true
          });
        }

      }).error(function(response) {
        $scope.status = UPDATE_FAILED;
        var errorMessage;
        if($scope.data.executeImmediately) {
          errorMessage = 'Could not update the suspension state of the process definition: ' + response.message;
        } else {
          'The update of the suspension state of the process definition could not be scheduled: ' + response.message;
        }
        Notifications.addError({
          status: 'Finished',
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
