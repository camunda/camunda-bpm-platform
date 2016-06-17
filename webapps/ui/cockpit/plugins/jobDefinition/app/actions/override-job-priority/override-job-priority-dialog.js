'use strict';

var angular = require('angular');

module.exports = [
  '$scope', '$q', 'Notifications', 'JobDefinitionResource', '$modalInstance', 'jobDefinition',
  function($scope,   $q,   Notifications,   JobDefinitionResource,   $modalInstance,   jobDefinition) {

    var SUCCESS = 'SUCCESS',
        FAILED = 'FAIL';

    $scope.status;
    $scope.setJobPriority = true;

    var data = $scope.data = {
      priority: jobDefinition.overridingJobPriority,
      includeJobs: false
    };

    $scope.$on('$routeChangeStart', function() {
      var response = {};
      response.status = $scope.status;
      $modalInstance.close(response);
    });

    $scope.hasOverridingJobPriority = function() {
      return jobDefinition.overridingJobPriority !== null && jobDefinition.overridingJobPriority !== undefined;
    };

    $scope.submit = function() {
      var setJobPriority = $scope.setJobPriority;
      if (!setJobPriority) {
        data = {};
      }

      JobDefinitionResource.setJobPriority({ 'id' : jobDefinition.id }, data,

        function() {
          $scope.status = SUCCESS;
          if (setJobPriority) {
            Notifications.addMessage({ 'status': 'Finished', 'message': 'Overriding the priority completed successfully.', 'exclusive': true });
          }
          else {
            Notifications.addMessage({ 'status': 'Finished', 'message': 'Clearing the priority completed successfully.', 'exclusive': true });
          }
        },

        function(error) {
          $scope.status = FAILED;
          if (setJobPriority) {
            Notifications.addError({ 'status': 'Finished', 'message': 'Overriding the priority was not successful: ' + error.data.message, 'exclusive': true });
          }
          else {
            Notifications.addError({ 'status': 'Finished', 'message': 'Clearing the priority was not successful: ' + error.data.message, 'exclusive': true });
          }
        }
      );
    };

    $scope.isValid = function() {
      var formScope = angular.element('[name="overrideJobPriorityForm"]').scope();
      return !$scope.setJobPriority || ((formScope && formScope.overrideJobPriorityForm) ? formScope.overrideJobPriorityForm.$valid : false);
    };

    $scope.close = function(status) {
      var response = {};
      response.status = status;
      $modalInstance.close(response);
    };

  }];
