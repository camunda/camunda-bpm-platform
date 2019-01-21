'use strict';

module.exports = [
  '$scope', '$location', 'Notifications', 'camAPI', '$uibModalInstance', 'incident', '$translate',
  function($scope,   $location,   Notifications,   camAPI,   $modalInstance,   incident, $translate) {

    var FINISHED = 'finished',
        PERFORM = 'performing',
        FAILED = 'failed';

    var ExternalTask = camAPI.resource('external-task');

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.incrementRetry = function() {
      $scope.status = PERFORM;

      ExternalTask.retries({
        id: incident.configuration,
        retries: 1
      }, function(error) {

        if (!error) {
          $scope.status = FINISHED;
          Notifications.addMessage({
            status: $translate.instant('PLUGIN_EXTERNAL_TASK_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_EXTERNAL_TASK_MESSAGE_1'),
            exclusive: true
          });
        } else {
          $scope.status = FAILED;
          Notifications.addError({
            status: $translate.instant('PLUGIN_EXTERNAL_TASK_STATUS_FINISHED'),
            message: $translate.instant('PLUGIN_EXTERNAL_TASK_MESSAGE_2', {message: error.message}),
            exclusive: true
          });
        }
      });
    };

    $scope.close = function(status) {
      $modalInstance.close(status);
    };
  }];
