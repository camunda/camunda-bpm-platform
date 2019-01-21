'use strict';

var angular = require('angular');

module.exports = [
  '$scope', '$q', 'Notifications', 'JobDefinitionResource', '$uibModalInstance', 'jobDefinition', '$translate',
  function($scope,   $q,   Notifications,   JobDefinitionResource,   $modalInstance,   jobDefinition, $translate) {

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
            Notifications.addMessage({ 'status': $translate.instant('PLUGIN_JOBDEFINITION_ACTION_STATUS_FINISHED'), 'message': $translate.instant('PLUGIN_JOBDEFINITION_ACTION_DIALOG_MSN_1'), 'exclusive': true });
          }
          else {
            Notifications.addMessage({ 'status': $translate.instant('PLUGIN_JOBDEFINITION_ACTION_STATUS_FINISHED'), 'message': $translate.instant('PLUGIN_JOBDEFINITION_ACTION_DIALOG_MSN_2'), 'exclusive': true });
          }
        },

        function(error) {
          $scope.status = FAILED;
          if (setJobPriority) {
            Notifications.addError({ 'status': $translate.instant('PLUGIN_JOBDEFINITION_ACTION_STATUS_FINISHED'), 'message': $translate.instant('PLUGIN_JOBDEFINITION_ACTION_DIALOG_ERR_1', { message: error.data.message }), 'exclusive': true });
          }
          else {
            Notifications.addError({ 'status': $translate.instant('PLUGIN_JOBDEFINITION_ACTION_STATUS_FINISHED'), 'message': $translate.instant('PLUGIN_JOBDEFINITION_ACTION_DIALOG_ERR_2', { message: error.data.message }), 'exclusive': true });
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
