  'use strict';

  module.exports = [
    '$scope', '$location', 'Notifications', 'camAPI', '$modalInstance', 'incident',
    function($scope,   $location,   Notifications,   camAPI,   $modalInstance,   incident) {

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
        }, function() {
          $scope.status = FINISHED;

          Notifications.addMessage({
            status: 'Finished',
            message: 'Incrementing the number of retries finished successfully.',
            exclusive: true
          });
        }, function(error) {
          $scope.status = FAILED;
          Notifications.addError({
            status: 'Finished',
            message: 'Incrementing the number of retries was not successful: ' + error.data.message,
            exclusive: true
          });
        });
      };

      $scope.close = function(status) {
        $modalInstance.close(status);
      };
    }];
