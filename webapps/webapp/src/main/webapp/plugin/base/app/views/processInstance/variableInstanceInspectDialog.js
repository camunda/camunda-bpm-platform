/* global ngDefine: false, angular: false */
ngDefine('cockpit.plugin.base.views', function(module) {
  'use strict';

  module.controller('VariableInstanceInspectController', [
          '$scope', '$location', 'Notifications', '$modalInstance', 'Uri', 'variableInstance',
  function($scope,   $location,   Notifications,   $modalInstance,   Uri,   variableInstance) {

    var BEFORE_CHANGE = 'beforeChange',
        CONFIRM_CHANGE = 'confirmChange',
        CHANGE_SUCCESS = 'changeSuccess';

    $scope.variableInstance = variableInstance;
    $scope.status = BEFORE_CHANGE;

    $scope.initialValue = JSON.stringify(variableInstance.value.object, null, 2);
    $scope.currentValue = $scope.initialValue;

    $scope.confirmed = false;

    function uploadComplete(parsedValue) {
      /* jshint validthis: true */
      var self = this;
      $scope.$apply(function(){
        if(self.status == 204) {
          $scope.status = CHANGE_SUCCESS;

          Notifications.addMessage({
            status: 'Success',
            message: 'Successfully updated the variable.'
          });

          angular.extend(variableInstance, {
            type: variableInstance.type,
            value: {
              type: variableInstance.value.type,
              object : parsedValue
            }
          });
        }
        else {
          $scope.status = BEFORE_CHANGE;

          Notifications.addError({
            status: 'Failed',
            message: 'Could not update variable: '+self.responseText,
            exclusive: ['type']
          });
        }
      });
    }

    $scope.hasChanged = function() {
      return $scope.initialValue != $scope.currentValue;
    };

    $scope.change = function () {

      if($scope.status == BEFORE_CHANGE) {
        $scope.status = CONFIRM_CHANGE;

      } else {

        var newValue = $scope.currentValue;
        var parsedValue;

        try {
          // check whether the user provided valid JSON.
          parsedValue = JSON.parse(newValue);
        } catch(e) {
          $scope.status = BEFORE_CHANGE;
          Notifications.addError({
            status: 'Variable',
            message: 'Could not parse json input: '+e,
            exclusive: true
          });
          return;
        }

        // create HTML 5 form upload
        var fd = new FormData();
        fd.append('data', new Blob([$scope.currentValue], {type : 'application/json'}));
        fd.append('type', variableInstance.value.type);

        var xhr = new XMLHttpRequest();
        xhr.addEventListener('load', function() {
          uploadComplete(parsedValue);
        }, false);
        xhr.open('POST', $scope.getVariableUploadUrl());
        xhr.send(fd);

      }

    };

    $scope.getVariableUploadUrl = function () {
      return Uri.appUri('engine://engine/:engine/execution/'+variableInstance.executionId+'/localVariables/'+variableInstance.name+'/data');
    };

    $scope.$on('$routeChangeStart', function () {
      $modalInstance.close($scope.status);
    });

    $scope.close = function (status) {
      $modalInstance.close(status);
    };
  }]);
});
