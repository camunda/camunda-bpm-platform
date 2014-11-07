define([
  'angular'
], function(
  angular
) {
  'use strict';

  var GROUP_TYPE = 'candidate';

  return [
    '$scope',
    '$translate',
    '$q',
    'Notifications',
    'camAPI',
  function(
    $scope,
    $translate,
    $q,
    Notifications,
    camAPI
  ) {
    // setup //////////////////////////////////////////////

    var Task = camAPI.resource('task');

    var task = null;

    var groupsChanged = false;

    var NEW_GROUP = { groupId : null, type: GROUP_TYPE };

    var newGroup = $scope.newGroup =  angular.copy(NEW_GROUP);

    var taskGroupsData = $scope.$parent.$parent.taskGroupsData;

    $scope._groups = [];

    var messages = {};
    $translate([
      'FAILURE',
      'INIT_GROUPS_FAILURE',
      'ADD_GROUP_FAILED',
      'REMOVE_GROUP_FAILED'
    ])
    .then(function(result) {
      messages.failure            = result.FAILURE;
      messages.initGroupsFailed   = result.INIT_GROUPS_FAILURE;
      messages.addGroupFailed     = result.ADD_GROUP_FAILED;
      messages.removeGroupFailed  = result.REMOVE_GROUP_FAILED;
    });

    // observe ////////////////////////////////////////////////////////

    // refresh list of groups
    taskGroupsData.changed('groups');

    $scope.modalGroupsState = taskGroupsData.observe('groups', function(groups) {
      $scope._groups = angular.copy(groups) || [];
      $scope.validateNewGroup();
    });

    taskGroupsData.observe('task', function (_task) {
      task = _task;
    });

    // actions ///////////////////////////////////////////////////////

    $scope.$watch('modalGroupsState.$error', function (error){
      if (error) {
        Notifications.addError({
          status: messages.failure,
          message: messages.initGroupsFailed,
          exclusive: true
        });
      }
    });

    $scope.addGroup = function () {
      var taskId = task.id;

      groupsChanged = true;

      delete newGroup.error;

      Task.identityLinksAdd(taskId, newGroup, function(err) {
        if (err) {
          return Notifications.addError({
            status: messages.failure,
            message: messages.addGroupFailed,
            exclusive: true
          });
        }

        $scope.taskGroupForm.$setPristine();

        newGroup = $scope.newGroup = angular.copy(NEW_GROUP);
        taskGroupsData.changed('groups');

      });
    };

    $scope.removeGroup = function(group) {
      var taskId = task.id;

      groupsChanged = true;

      Task.identityLinksDelete(taskId, group, function(err) {
        if (err) {
          return Notifications.addError({
            status: messages.failure,
            message: messages.removeGroupFailed,
            exclusive: true
          });
        }

        taskGroupsData.changed('groups');
      });
    };

    $scope.validateNewGroup = function () {
      delete newGroup.error;
      $scope.taskGroupForm.newGroup.$setValidity('duplicate', true);

      var newGroupId = newGroup.groupId;

      if (newGroupId) {
        for(var i = 0, currentGroup; !!(currentGroup = $scope._groups[i]); i++) {
          if (newGroupId === currentGroup.groupId) {
            newGroup.error = { message: 'DUPLICATE_GROUP' };

            $scope.taskGroupForm.newGroup.$setValidity('duplicate', false);
          }
        }
      }
    };

    $scope.isValid = function () {
      if (!newGroup.groupId || newGroup.error) {
        return false;
      }

      return true;
    };

  }];

});
