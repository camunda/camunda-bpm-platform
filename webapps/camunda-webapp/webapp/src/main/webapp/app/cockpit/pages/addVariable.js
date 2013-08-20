ngDefine('cockpit.pages', function(module, $) {

  var AddVariableController = [ '$scope', '$http', 'Uri', 'Notifications', 'dialog', 'processInstance', 'processData', 
                        function($scope, $http, Uri, Notifications, dialog, processInstance, processData) {

    $scope.variableTypes = [
      'String',
      'Boolean',
      'Short',
      'Integer',
      'Long',
      'Double',
      'Date'
    ];

    var newVariable = $scope.newVariable = {
      name: null,
      type: 'String',
      value: null
    };

    var PERFORM_SAVE = 'PERFORM_SAVE',
        SUCCESS = 'SUCCESS',
        FAIL = 'FAIL';

    $scope.$on('$routeChangeStart', function () {
      dialog.close($scope.status);
    });

    $scope.close = function () {
      dialog.close($scope.status);
    };

    var isValid = $scope.isValid = function() {
      return $scope.addVariableForm.$valid;
    };

    $scope.save = function () {
      if (!isValid()) {
        return;
      }

      $scope.status = PERFORM_SAVE;

      var data = angular.extend({}, newVariable),
          name = data.name;

      delete data.name;

      $http.put(Uri.appUri('engine://engine/:engine/process-instance/' + processInstance.id + '/variables/' + name), data).success(function (data) {
        $scope.status = SUCCESS;

        Notifications.addMessage({'status': 'Finished', 'message': 'Added the variable', 'exclusive': true }); 

      }).error(function (data) {
        $scope.status = FAIL;

        Notifications.addError({'status': 'Finished', 'message': 'Could not add the new variable: ' + data.message, 'exclusive': true });
      });
    };
  }];

  module.controller('AddVariableController', AddVariableController);

});