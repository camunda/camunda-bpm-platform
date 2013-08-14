ngDefine('cockpit.pages', function(module, $) {

  function AddVariableController ($scope, $http, Uri, RequestStatus, Notifications) {

    var processInstance = $scope.processInstance;

    var newVariable = $scope.newVariable = {
      name: null,
      type: 'String',
      value: null
    };

    var PERFORM_SAVE = 'PERFORM_SAVE',
        SUCCESS = 'SUCCESS',
        FAIL = 'FAIL';

    $scope.$on('$routeChangeStart', function () {
      $scope.addVariableDialog.close();
    });

    $scope.close = function () {
      if ($scope.status === 'SUCCESS') {
        $scope.newVariableAdded();
      }

      $scope.addVariableDialog.close();
    };

    var isValid = $scope.isValid = function() {
      return $scope.addVariableForm.$valid;
    };

    $scope.save = function () {
      if (!isValid()) {
        return;
      }

      $scope.status = PERFORM_SAVE;
      RequestStatus.setBusy(true);

      var data = angular.extend({}, newVariable),
          name = data.name;

      delete data.name;

      $http.put(Uri.appUri('engine://engine/:engine/process-instance/' + processInstance.id + '/variables/' + name), data).success(function (data) {
        $scope.status = SUCCESS;
        RequestStatus.setBusy(false);

        Notifications.addMessage({'status': 'Finished', 'message': 'Adding new variable to the process instance finished.', 'exclusive': true}); 

      }).error(function (data) {
        $scope.status = FAIL;
        RequestStatus.setBusy(false);

        Notifications.addError({'status': 'Finished', 'message': 'Adding new variable to the process instance failed: ' + data.message, 'exclusive': true});

      });
    };
  };

  module.controller('AddVariableController', [ '$scope', '$http', 'Uri', 'RequestStatus', 'Notifications', AddVariableController ]);

});