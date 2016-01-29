'use strict';

var angular = require('angular');

  module.exports = [
    '$scope',
    '$translate',
    'Notifications',
    'camAPI',
  function(
    $scope,
    $translate,
    Notifications,
    camAPI
  ) {

    var NEW_TASK = {
      name: null,
      assignee: null,
      description: null,
      priority: 50 // default value
    };

    var Task = camAPI.resource('task');

    var task = $scope.task = angular.copy(NEW_TASK);

    var _form = null;

    $scope.setNewTaskForm = function (innerForm) {
      _form = innerForm;
    }

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    var isValid = $scope.isValid = function () {
      return _form && _form.$valid;
    };

    $scope.save = function () {
      if (!isValid()) {
        return;
      }

      Task.create(task, function (err) {
        if (err) {
          $translate('TASK_SAVE_ERROR').then(function(translated) {
            Notifications.addError({
              status: translated,
              message: (err ? err.message : ''),
              exclusive: true,
              scope: $scope
            });
          });
        }
        else {
          $scope.$close();
        }
      });
    };

  }];
