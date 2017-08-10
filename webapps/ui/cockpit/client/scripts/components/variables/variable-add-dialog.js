'use strict';

var angular = require('angular');
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/variable-add-dialog.html', 'utf8');

var Controller = [
  '$http',
  '$modalInstance',
  '$scope',
  'Notifications',
  'Uri',
  'instance',
  'isProcessInstance',
  'fixDate',
  'typeUtils',
  function(
    $http,
    $modalInstance,
    $scope,
    Notifications,
    Uri,
    instance,
    isProcessInstance,
    fixDate,
    typeUtils
  ) {

    $scope.isProcessInstance = isProcessInstance;

    $scope.variableTypes = [
      'String',
      'Boolean',
      'Short',
      'Integer',
      'Long',
      'Double',
      'Date',
      'Null',
      'Object',
      'Json',
      'Xml'
    ];

    var newVariable = $scope.newVariable = {
      name: null,
      type: 'String',
      value: null
    };

    var PERFORM_SAVE = 'PERFORM_SAVE',
        SUCCESS = 'SUCCESS',
        FAIL = 'FAIL';

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.close = function() {
      $modalInstance.close($scope.status);
    };

    $scope.getFormScope = function() {
      return angular.element('[name="addVariableForm"]').scope();
    };

    $scope.customValidator = function(type, value) {
      if(['Json', 'Xml'].indexOf(type) > -1) {
        var valid = typeUtils.isType(value, type);
        $scope.getFormScope().addVariableForm.$setValidity('customValidation', valid);
      }
    };

    var isValid = $scope.isValid = function() {
      // that's a pity... I do not get why,
      // but getting the form scope is.. kind of random
      // m2c: it has to do with the `click event`
      // Hate the game, not the player
      var formScope = $scope.getFormScope();
      return (formScope && formScope.addVariableForm) ? formScope.addVariableForm.$valid : false;
    };

    $scope.$watch('newVariable.value', function(newValue) {
      var type = $scope.newVariable.type;
      return $scope.customValidator(type, newValue);
    });

    $scope.$watch('newVariable.type', function(newType) {
      var value = $scope.newVariable.value;
      return $scope.customValidator(newType, value);
    });

    $scope.save = function() {
      if (!isValid()) {
        return;
      }

      $scope.status = PERFORM_SAVE;

      var data = angular.extend({}, newVariable),
          name = data.name;

      delete data.name;

      if(data.type === 'Date') {
        data.value = fixDate(data.value);
      }

      $http
      .put(Uri.appUri('engine://engine/:engine/'+(isProcessInstance ? 'process' : 'case')+'-instance/'+instance.id+'/variables/'+name), data)
      .success(function() {
        $scope.status = SUCCESS;

        Notifications.addMessage({
          status: 'Finished',
          message: 'Added the variable',
          exclusive: true
        });
      }).error(function(data) {
        $scope.status = FAIL;

        Notifications.addError({
          status: 'Finished',
          message: 'Could not add the new variable: ' + data.message,
          exclusive: true
        });
      });
    };
  }];

module.exports = {
  template: template,
  controller: Controller
};

