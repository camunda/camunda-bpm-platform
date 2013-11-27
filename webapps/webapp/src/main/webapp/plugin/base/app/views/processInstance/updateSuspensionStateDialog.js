ngDefine('cockpit.plugin.base.views', function(module, $) {

  var Controller = [ '$scope', '$http', '$filter', 'Uri', 'Notifications', 'dialog', 'processInstance', 'processData',
      function($scope, $http, $filter, Uri, Notifications, dialog, processInstance, processData) {

    var BEFORE_UPDATE = 'BEFORE_UPDATE',
        PERFORM_UPDATE = 'PERFORM_UDPATE',
        UPDATE_SUCCESS = 'SUCCESS',
        UPDATE_FAILED = 'FAIL';

    $scope.processInstance = processInstance;

    $scope.status = BEFORE_UPDATE;

    $scope.$on('$routeChangeStart', function () {
      dialog.close($scope.status);
    });

    $scope.updateSuspensionState = function () {
      $scope.status = PERFORM_UPDATE;

      var data = {};

      data.suspended = !processInstance.suspended;

      $http.put(Uri.appUri('engine://engine/:engine/process-instance/' + processInstance.id + '/suspended/'), data).success(function (data) {
        $scope.status = UPDATE_SUCCESS;

        Notifications.addMessage({'status': 'Finished', 'message': 'Updated the suspension state of the process instance.', 'exclusive': true });  

      }).error(function (data) {
        $scope.status = UPDATE_FAILED;

        Notifications.addError({'status': 'Finished', 'message': 'Could not update the suspension state of the process instance: ' + data.message, 'exclusive': true });

      });
    }

    $scope.close = function (status) {
      var response = {};

      response.status = status;
      response.suspended = !processInstance.suspended;

      dialog.close(response);
    };      

  }];

  module.controller('UpdateProcessInstanceSuspensionStateController', Controller);

});
