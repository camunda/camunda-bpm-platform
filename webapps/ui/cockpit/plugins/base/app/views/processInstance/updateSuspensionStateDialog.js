  'use strict';

  module.exports = [
    '$scope', '$http', '$filter', 'Uri', 'Notifications', '$modalInstance', 'processInstance',
    function($scope,   $http,   $filter,   Uri,   Notifications,   $modalInstance,   processInstance) {

      var BEFORE_UPDATE = 'BEFORE_UPDATE',
          PERFORM_UPDATE = 'PERFORM_UDPATE',
          UPDATE_SUCCESS = 'SUCCESS',
          UPDATE_FAILED = 'FAIL';

      $scope.processInstance = processInstance;

      $scope.status = BEFORE_UPDATE;

      $scope.$on('$routeChangeStart', function() {
        $modalInstance.close($scope.status);
      });

      $scope.updateSuspensionState = function() {
        $scope.status = PERFORM_UPDATE;

        var data = {};

        data.suspended = !processInstance.suspended;

        $http.put(Uri.appUri('engine://engine/:engine/process-instance/' + processInstance.id + '/suspended/'), data).success(function() {
          $scope.status = UPDATE_SUCCESS;

          Notifications.addMessage({
            status: 'Finished',
            message: 'Updated the suspension state of the process instance.',
            exclusive: true
          });

        }).error(function(data) {
          $scope.status = UPDATE_FAILED;

          Notifications.addError({
            status: 'Finished',
            message: 'Could not update the suspension state of the process instance: ' + data.message,
            exclusive: true
          });
        });
      };

      $scope.close = function(status) {
        var response = {};

        response.status = status;
        response.suspended = !processInstance.suspended;

        $modalInstance.close(response);
      };

    }];
