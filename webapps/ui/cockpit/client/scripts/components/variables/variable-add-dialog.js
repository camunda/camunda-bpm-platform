'use strict';

var angular = require('angular');
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/variable-add-dialog.html', 'utf8');

var Controller = [
  '$uibModalInstance',
  '$scope',
  'Notifications',
  'Uri',
  'instance',
  'isProcessInstance',
  'fixDate',
  '$translate',
  'camAPI',
  function(
    $modalInstance,
    $scope,
    Notifications,
    Uri,
    instance,
    isProcessInstance,
    fixDate,
    $translate,
    camAPI
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

    var processInstance = camAPI.resource('process-instance');
    var caseInstance = camAPI.resource('case-instance');

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    $scope.close = function() {
      $modalInstance.close($scope.status);
    };



    var isValid = $scope.isValid = function() {
      // that's a pity... I do not get why,
      // but getting the form scope is.. kind of random
      // m2c: it has to do with the `click event`
      // Hate the game, not the player
      var formScope = angular.element('[name="addVariableForm"]').scope();
      return (formScope && formScope.addVariableForm) ? formScope.addVariableForm.$valid : false;
    };


    $scope.save = function() {
      if (!isValid()) {
        return;
      }

      $scope.status = PERFORM_SAVE;

      var data = angular.extend({}, newVariable);

      if(data.type === 'Date') {
        data.value = fixDate(data.value);
      }

      var instanceAPI = (isProcessInstance ? processInstance : caseInstance);
      instanceAPI
        .setVariable(instance.id, data)
        .then(function() {
          $scope.status = SUCCESS;

          Notifications.addMessage({
            status: $translate.instant('VARIABLE_ADD_MESSAGE_STATUS_FINISHED'),
            message: $translate.instant('VARIABLE_ADD_MESSAGE_MESSAGE_ADD', { name: data.name }),
            exclusive: true
          });
        })
        .catch(function(data) {
          $scope.status = FAIL;

          Notifications.addError({
            status: $translate.instant('VARIABLE_ADD_MESSAGE_STATUS_FINISHED'),
            message: $translate.instant('VARIABLE_ADD_MESSAGE_MESSAGE_ERROR', {message: data.message}),
            exclusive: true
          });
        });
    };
  }];

module.exports = {
  template: template,
  controller: Controller
};

