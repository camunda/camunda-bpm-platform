ngDefine('cockpit.plugin.base.views', function(module, $) {

  var Controller = [ '$scope', '$http', '$filter', 'Uri', 'Notifications', 'dialog', 'processDefinition', 'processData',
      function($scope, $http, $filter, Uri, Notifications, dialog, processDefinition, processData) {

    var BEFORE_UPDATE = 'BEFORE_UPDATE',
        PERFORM_UPDATE = 'PERFORM_UDPATE',
        UPDATE_SUCCESS = 'SUCCESS',
        UPDATE_FAILED = 'FAIL';

    var dateFilter = $filter('date'),
        dateFormat = 'yyyy-MM-dd\'T\'HH:mm:ss';


    $scope.processDefinition = processDefinition;

    $scope.status = BEFORE_UPDATE;

    $scope.includeInstances = true;
    $scope.executeImmediately = true;
    $scope.executionDate = dateFilter(Date.now(), dateFormat);

    $scope.$on('$routeChangeStart', function () {
      dialog.close($scope.status);
    });

    $scope.updateSuspensionState = function () {
      $scope.status = PERFORM_UPDATE;

      var data = {};

      data.suspended = !processDefinition.suspended;
      data.includeProcessInstances = $scope.includeInstances;
      data.executionDate = !$scope.executeImmediately ? $scope.executionDate : null;

      $http.put(Uri.appUri('engine://engine/:engine/process-definition/' + processDefinition.id + '/suspended/'), data).success(function (data) {
        $scope.status = UPDATE_SUCCESS;

        if ($scope.executeImmediately) {
          Notifications.addMessage({'status': 'Finished', 'message': 'Updated the suspension state of the process definition.', 'exclusive': true });  
        } else {
          Notifications.addMessage({'status': 'Finished', 'message': 'The update of the suspension state of the process definition has been scheduled.', 'exclusive': true });  
        }

      }).error(function (data) {
        $scope.status = UPDATE_FAILED;

        if ($scope.executeImmediately) {
          Notifications.addError({'status': 'Finished', 'message': 'Could not update the suspension state of the process definition: ' + data.message, 'exclusive': true });
        } else {
          Notifications.addMessage({'status': 'Finished', 'message': 'The update of the suspension state of the process definition could not be scheduled: ' + data.message, 'exclusive': true });  
        }
      });
    }

    $scope.isValid = function () {
      if (!$scope.executeImmediately) {
        return $scope.updateSuspensionStateForm.$valid;    
      }
      return true;
    };

    $scope.close = function (status) {
      var response = {};

      response.status = status;
      response.suspended = !processDefinition.suspended;
      response.executeImmediately = $scope.executeImmediately;
      response.executionDate = $scope.executionDate;

      dialog.close(response);
    };      

  }];

  module.controller('UpdateProcessDefinitionSuspensionStateController', Controller);

});
