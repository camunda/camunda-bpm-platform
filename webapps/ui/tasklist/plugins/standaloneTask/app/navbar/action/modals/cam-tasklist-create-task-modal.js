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
      tenantId: null,
      description: null,
      priority: 50 // default value
    };

    var Task = camAPI.resource('task');
    var Tenant = camAPI.resource('tenant');

    var task = $scope.task = angular.copy(NEW_TASK);

    function getTenants() {
      var queryParams = {
        userMember : $scope.authentication.name,
        includingGroupsOfUser : true
      };

      Tenant.list(queryParams, function(err, res) {
        if (res && res.length > 0) {
          task.tenantId = res[0].id;
          if (res.length > 1) {
            $scope.tenants = res;
          }
        }
      });
    }

    var _form = null;

    getTenants();

    $scope.setNewTaskForm = function(innerForm) {
      _form = innerForm;
    };

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    var isValid = $scope.isValid = function() {
      return _form && _form.$valid;
    };

    $scope.save = function() {
      if (!isValid()) {
        return;
      }

      Task.create(task, function(err) {
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
