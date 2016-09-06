'use strict';

var angular = require('angular');
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/variable-inspect-dialog.html', 'utf8');

var Controller = [
  '$http',
  '$modalInstance',
  '$scope',
  'Notifications',
  'Uri',
  'basePath',
  'history',
  'readonly',
  'variable',
  function(
    $http,
    $modalInstance,
    $scope,
    Notifications,
    Uri,
    basePath,
    history,
    readonly,
    variable
  ) {

    var BEFORE_CHANGE = 'beforeChange',
        CONFIRM_CHANGE = 'confirmChange',
        CHANGE_SUCCESS = 'changeSuccess';

    $scope.selectedTab = 'serialized';
    $scope.status = BEFORE_CHANGE;

    $scope.variable = variable;
    $scope.readonly = readonly;

    $scope.currentValue = angular.copy(variable.value);

    var initialDeserializedValue;


    $scope.$on('$routeChangeStart', function() {
      $modalInstance.dismiss();
    });


    $scope.selectTab = function(tab) {
      $scope.selectedTab = tab;
      // reset changed state
      $scope.currentValue = angular.copy(variable.value);
      $scope.currentDeserializedValue = angular.copy(initialDeserializedValue);
      $scope.status = BEFORE_CHANGE;
    };


    $scope.typeIn = function(formScope, type) {
      if(isSerializedTab(type)) {
        $scope.currentValue = formScope.currentValue;
      }
      else {
        $scope.currentDeserializedValue = formScope.currentDeserializedValue;
      }

      $scope.status = hasChanged(type) ? CONFIRM_CHANGE : BEFORE_CHANGE;
    };


    var hasChanged = $scope.hasChanged = function(type) {
      if(isSerializedTab(type)) {
        return variable.value !== $scope.currentValue;
      }
      else {
        return initialDeserializedValue != $scope.currentDeserializedValue;
      }
    };


    $scope.change = function() {

      var updateDeserialized = !isSerializedTab($scope.selectedTab);
      var newValue = updateDeserialized ? $scope.currentDeserializedValue : $scope.currentValue;

      if(variable.valueInfo.serializationDataFormat === 'application/json' || updateDeserialized) {
        try {
          // check whether the user provided valid JSON.
          JSON.parse(newValue);
        }
        catch(e) {
          $scope.status = BEFORE_CHANGE;
          Notifications.addError({
            status: 'Variable',
            message: 'Could not parse JSON input: ' + e,
            exclusive: true
          });
          return;
        }
      }

      !updateDeserialized ? updateSerializedValue(variable, newValue) : updateDeserializedValue(variable, newValue);

    };


    // load deserialized value:
    loadDeserializedValue();


    function isSerializedTab(tab) {
      return tab === 'serialized';
    }

    function updateSerializedValue(variable, newValue) {
      var variableUpdate = {
        type: variable.type,
        value: newValue,
        valueInfo: variable.valueInfo
      };

      $http({
        method: 'PUT',
        url: Uri.appUri(basePath),
        data: variableUpdate
      })
      .success(function() {
        $scope.status = CHANGE_SUCCESS;
        addMessage(variable);
      })
      .error(function() {
        $scope.status = BEFORE_CHANGE;
        addError(variable);
      });
    }

    function updateDeserializedValue(variable, newValue) {

      function callback(xhr) {

        $scope.$apply(function() {

          if(xhr.status === 204) {
            $scope.status = CHANGE_SUCCESS;
            addMessage(variable);
          }
          else {
            $scope.status = BEFORE_CHANGE;
            addError(variable);
          }

        });
      }

      // update deserialized
      // create HTML 5 form upload
      var fd = new FormData();
      fd.append('data', new Blob([ newValue ], { type : 'application/json' }));
      fd.append('type', variable.valueInfo.objectTypeName);

      var xhr = new XMLHttpRequest();
      xhr.addEventListener('load', function() {
        callback(xhr);
      }, false);
      xhr.open('POST', Uri.appUri( basePath + '/data'));
      xhr.send(fd);
    }

    function loadDeserializedValue() {

      $http({
        method: 'GET',
        url: Uri.appUri('engine://engine/:engine/' + (history ? 'history/' : '') + 'variable-instance/' + variable.id)
      })
      .success(function(data) {
        if (!data.errorMessage) {
          initialDeserializedValue = JSON.stringify(data.value);
          $scope.currentDeserializedValue = angular.copy(initialDeserializedValue);
        }
        else {
          $scope.deserializationError = data.errorMessage;
        }
      }).error(function(err) {
        $scope.deserializationError = err.message;
      });

    }

    function addError(variable) {
      Notifications.addError({
        status: 'Variable',
        message: 'The variable \'' + variable.name + '\' could not be changed successfully.',
        exclusive: true
      });
    }

    function addMessage(variable) {
      Notifications.addMessage({
        status: 'Variable',
        message: 'The variable \'' + variable.name + '\' has been changed successfully.'
      });
    }

  }];

module.exports = {
  template: template,
  controller: Controller
};
