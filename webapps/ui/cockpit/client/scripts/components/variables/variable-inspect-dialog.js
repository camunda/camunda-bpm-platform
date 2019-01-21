'use strict';

var angular = require('angular');
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/variable-inspect-dialog.html', 'utf8');

var Controller = [
  '$http',
  '$uibModalInstance',
  '$scope',
  'Notifications',
  'Uri',
  'basePath',
  'history',
  'readonly',
  'variable',
  '$translate',
  function(
    $http,
    $modalInstance,
    $scope,
    Notifications,
    Uri,
    basePath,
    history,
    readonly,
    variable,
    $translate
  ) {

    var BEFORE_CHANGE = 'beforeChange',
        CONFIRM_CHANGE = 'confirmChange',
        CHANGE_SUCCESS = 'changeSuccess';


    /**
     * Check if the json/xml field is valid
     */
    function isValidJsonXml() {
      return angular.element('[name="jsonXmlForm"]').scope().jsonXmlForm.$valid;
    }

    $scope.selectedTab = 'serialized';
    $scope.status = BEFORE_CHANGE;

    $scope.variable = angular.copy(variable);
    $scope.isJsonOrXml = variable.type && ( variable.type.toLowerCase() === 'json' || variable.type.toLowerCase() === 'xml' );
    $scope.readonly = readonly;

    $scope.currentValue = angular.copy(variable.value);

    var initialDeserializedValue;

    $scope.isChangeDisabled = function() {
      return ( $scope.isJsonOrXml ? !isValidJsonXml(): $scope.status !== 'beforeChange') || !hasChanged();
    };

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.dismiss();
    });


    $scope.selectTab = function(tab) {
      $scope.selectedTab = tab;
      // reset changed state
      $scope.currentValue = angular.copy($scope.variable.value);
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
      if($scope.isJsonOrXml || isSerializedTab(type)) {
        return $scope.variable.value !== $scope.currentValue;
      } else {
        return initialDeserializedValue != $scope.currentDeserializedValue;
      }
    };


    $scope.change = function() {

      var updateDeserialized = !isSerializedTab($scope.selectedTab);
      var newValue = updateDeserialized ? $scope.currentDeserializedValue : $scope.currentValue;
      if($scope.isJsonOrXml) {
        newValue = $scope.variable.value;
      }

      if(variable.valueInfo.serializationDataFormat === 'application/json' || updateDeserialized) {
        try {
          // check whether the user provided valid JSON.
          JSON.parse(newValue);
        }
        catch(e) {
          $scope.status = BEFORE_CHANGE;
          Notifications.addError({
            status: $translate.instant('VARIABLE_INSPECT_VARIABLE'),
            message: $translate.instant('VARIABLE_INSPECT_MESSAGE_ERR_1', { exception: e.message }),
            exclusive: true
          });
          return;
        }
      }

      !updateDeserialized ? updateValue($scope.variable, newValue) : updateDeserializedValue($scope.variable, newValue);

    };


    // load deserialized value:
    if(!$scope.isJsonOrXml) {
      loadDeserializedValue();
    }


    function isSerializedTab(tab) {
      return tab === 'serialized';
    }

    function updateValue(variable, newValue) {
      var variableUpdate = {
        type: variable.type,
        value: newValue,
        valueInfo: variable.valueInfo
      };

      if(!$scope.isJsonOrXml) {
        variableUpdate.valueInfo = variable.valueInfo;
      }

      $http({
        method: 'PUT',
        url: Uri.appUri(basePath),
        data: variableUpdate
      })
      .then(function() {
        $scope.status = CHANGE_SUCCESS;
        addMessage(variable);
      })
      .catch(function() {
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
      .then(function(data) {
        data = data.data;
        if (!data.errorMessage) {
          initialDeserializedValue = JSON.stringify(data.value);
          $scope.currentDeserializedValue = angular.copy(initialDeserializedValue);
        }
        else {
          $scope.deserializationError = data.errorMessage;
        }
      }).catch(function(err) {
        $scope.deserializationError = err.message;
      });

    }

    function addError(variable) {
      Notifications.addError({
        status: $translate.instant('VARIABLE_INSPECT_VARIABLE'),
        message: $translate.instant('VARIABLE_INSPECT_MESSAGE_ERR_2', { name : variable.name }),
        exclusive: true
      });
    }

    function addMessage(variable) {
      Notifications.addMessage({
        status: $translate.instant('VARIABLE_INSPECT_VARIABLE'),
        message: $translate.instant('VARIABLE_INSPECT_MESSAGE_ADD', { name : variable.name })
      });
    }

  }];

module.exports = {
  template: template,
  controller: Controller
};
