'use strict';

var angular = require('angular');

module.exports = [
  '$scope', '$location', '$http', 'Notifications', '$modalInstance', 'Uri', 'variableInstance',
  function($scope,   $location,   $http,   Notifications,   $modalInstance,   Uri,   variableInstance) {

    var BEFORE_CHANGE = 'beforeChange',
        CONFIRM_CHANGE = 'confirmChange',
        CHANGE_SUCCESS = 'changeSuccess';

    $scope.selectedTab = 'serialized';

    $scope.variableInstance = variableInstance;
    $scope.status = BEFORE_CHANGE;

    $scope.initialValue = variableInstance.value;
    $scope.objectType = variableInstance.valueInfo.objectTypeName;
    $scope.dataFormat = variableInstance.valueInfo.serializationDataFormat;

    $scope.initialValueDeserialized = null;
    $scope.deserializationError = null;

    $scope.currentValue = angular.copy($scope.initialValue);
    $scope.currentValueDeserialized = null;

    $scope.confirmed = false;

    function uploadComplete() {
      var self = $scope.xhr;
      $scope.$apply(function() {
        if(self.status == 204) {
          $scope.status = CHANGE_SUCCESS;

          Notifications.addMessage({
            status: 'Success',
            message: 'Successfully updated the variable.'
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
        // cleanup
        delete $scope.xhr;
      });
    }

    $scope.typeIn = function(formScope, type) {
      if(type === 'serialized') {
        $scope.currentValue = formScope.currentValue;
      }
      else {
        $scope.currentValueDeserialized = formScope.currentValueDeserialized;
      }

      if ($scope.hasChanged(type)) {
        $scope.status = CONFIRM_CHANGE;
      }
      else {
        $scope.status = BEFORE_CHANGE;
      }
    };

    $scope.hasChanged = function(type) {
      if(type === 'serialized') {
        return $scope.initialValue != $scope.currentValue;
      }
      else {
        return $scope.initialValueDeserialized != $scope.currentValueDeserialized;
      }
    };

    $scope.change = function() {
      if($scope.status == BEFORE_CHANGE) {
        $scope.status = CONFIRM_CHANGE;
      }
      else {
        var newValue;
        var updateDeserialized = false;

        if($scope.selectedTab === 'serialized') {
          newValue = $scope.currentValue;
        }
        else {
          newValue = $scope.currentValueDeserialized;
          updateDeserialized = true;
        }

        if($scope.dataFormat === 'application/json' || updateDeserialized) {
          try {
            // check whether the user provided valid JSON.
            JSON.parse(newValue);
          } catch(e) {
            $scope.status = BEFORE_CHANGE;
            Notifications.addError({
              status: 'Variable',
              message: 'Could not parse JSON input: '+e,
              exclusive: true
            });
            return;
          }
        }

        if(!updateDeserialized) {
          // update serialized

          var variableUpdate = {
            type: variableInstance.type,
            value: newValue,
            valueInfo: variableInstance.valueInfo
          };

          $http({method: 'PUT', url: $scope.getObjectVariablePutUrl(), data: variableUpdate})
            .success(function() {
              $scope.status = CHANGE_SUCCESS;

              Notifications.addMessage({
                status: 'Success',
                message: 'Successfully updated the variable.'
              });

            })
            .error(function(data) {
              $scope.status = BEFORE_CHANGE;

              Notifications.addError({
                status: 'Failed',
                message: 'Could not update variable: '+data,
                exclusive: ['type']
              });
            });
        }
        else {
          // update deserialized
          // create HTML 5 form upload
          var fd = new FormData();
          fd.append('data', new Blob([$scope.currentValueDeserialized], {type : 'application/json'}));
          fd.append('type', variableInstance.valueInfo.objectTypeName);

          var xhr = $scope.xhr = new XMLHttpRequest();
          xhr.addEventListener('load', function() {
            uploadComplete(newValue);
          }, false);
          xhr.open('POST', $scope.getSerializableVariableUploadUrl());
          xhr.send(fd);
        }


      }

    };

    // load deserialized value:
    $http({
      method: 'GET',
      url: Uri.appUri('engine://engine/:engine/variable-instance/'+variableInstance.id)
    }).success(function(data) {
      if(!data.errorMessage) {
        $scope.initialValueDeserialized = JSON.stringify(data.value);
        $scope.currentValueDeserialized = angular.copy($scope.initialValueDeserialized);
      }
      else {
        $scope.deserializationError = data.errorMessage;
      }
    }).error(function(data) {
      $scope.deserializedValue = data;
    });

    $scope.selectTab = function(tab) {
      $scope.selectedTab = tab;
      // reset changed state
      $scope.currentValue = angular.copy($scope.initialValue);
      $scope.currentValueDeserialized = angular.copy($scope.initialValueDeserialized);
      $scope.status = BEFORE_CHANGE;
    };

    $scope.getSerializableVariableUploadUrl = function() {
      return Uri.appUri('engine://engine/:engine/execution/'+variableInstance.executionId+'/localVariables/'+variableInstance.name+'/data');
    };

    $scope.getObjectVariablePutUrl = function() {
      return Uri.appUri('engine://engine/:engine/execution/'+variableInstance.executionId+'/localVariables/'+variableInstance.name);
    };

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.dismiss();
    });

  }];
