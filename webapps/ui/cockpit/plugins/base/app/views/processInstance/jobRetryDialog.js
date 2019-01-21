  'use strict';

  module.exports = [
    '$scope', '$location', 'Notifications', 'JobResource', '$uibModalInstance', 'incident', '$translate',
    function($scope,   $location,   Notifications,   JobResource,   $modalInstance,   incident, $translate) {

      var FINISHED = 'finished',
          PERFORM = 'performing',
          FAILED = 'failed';

      $scope.$on('$routeChangeStart', function() {
        $modalInstance.close($scope.status);
      });

      $scope.incrementRetry = function() {
        $scope.status = PERFORM;

        JobResource.setRetries({
          id: incident.configuration
        }, {
          retries: 1
        }, function() {
          $scope.status = FINISHED;

          Notifications.addMessage({
            status: $translate.instant('PLUGIN_JOB_RETRY_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_JOB_RETRY_MESSAGE_1'),
            exclusive: true
          });
        }, function(error) {
          $scope.status = FAILED;
          Notifications.addError({
            status: $translate.instant('PLUGIN_JOB_RETRY_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_JOB_RETRY_ERROR_1', { message: error.data.message }),
            exclusive: true
          });
        });
      };

      $scope.close = function(status) {
        $modalInstance.close(status);
      };
    }];
