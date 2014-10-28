define([
  'angular'
], function(angular) {
  'use strict';

  var GROUP_TYPE = 'candidate',
      OPERATION_REMOVE = 'REMOVE',
      OPERATION_ADD = 'ADD',
      ACTION_STATE_PERFORM = 'performing',
      ACTION_STATE_SUCCESS = 'successful',
      ACTION_STATE_FAILED = 'failed';


  function removeArrayItem(arr, delta) {
    var newArr = [];
    for (var key in arr) {
      if (key != delta) {
        newArr.push(arr[key]);
      }
    }
    return newArr;
  }

  return [
    '$scope',
    '$translate',
    '$q',
    'Notifications',
    'camAPI',
    'taskMetaData',
  function(
    $scope,
    $translate,
    $q,
    Notifications,
    camAPI,
    taskMetaData
  ) {

    // setup //////////////////////////////////////////////

    var Task = camAPI.resource('task');

    var task = null;

    var NEW_GROUP = { groupId : null, type: GROUP_TYPE };

    var taskGroupsData = taskMetaData.newChild($scope);

    var actions = $scope.actions = [];

    var finishedWithFailures = false;

    $scope.EDIT_GROUPS = true;
    $scope._groups = [];

    $scope.operation = 'GROUPS_ADD';

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    var messages = {};
    $translate([
      'FAILURE',
      'INIT_GROUPS_FAILURE',
      'FINISHED',
      'UPDATE_GROUPS_SUCCESS',
      'UPDATE_GROUPS_FAILED'
    ])
    .then(function(result) {
      messages.failure             = result.FAILURE;
      messages.initGroupsFailed    = result.INIT_GROUPS_FAILURE;
      messages.finished            = result.FINISHED;
      messages.updateGroupsSuccess = result.UPDATE_GROUPS_SUCCESS;
      messages.updateGroupsFailed  = result.UPDATE_GROUPS_FAILED;
    });

    // observe ////////////////////////////////////////////////////////

    // refresh list of groups
    taskGroupsData.changed('groups');

    $scope.modalGroupsState = taskGroupsData.observe('groups', function(groups) {
      if (groups && groups.length) {
        $scope.operation = 'GROUPS_EDIT';
      }
      $scope._groups = angular.copy(groups) || [];
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

    function addAction(group, operation) {
      actions.push({
        group: group,
        operation: operation
      });
    }

    $scope.addGroup = function () {
      var newGroup = angular.copy(NEW_GROUP);

      addAction(newGroup, OPERATION_ADD);

      $scope._groups.push(newGroup);
    };

    $scope.removeGroup = function(delta) {
      var group = $scope._groups[delta];

      addAction(group, OPERATION_REMOVE);

      $scope._groups = removeArrayItem($scope._groups, delta);

      validateGroups();
    };

    var validateGroups = $scope.validateGroups = function () {
      for(var i = 0, group; !!(group = $scope._groups[i]); i++) {
        validateGroup(group);
      }
    };

    function validateGroup(group) {
      delete group.error;

      var groupId = group.groupId;

      if (groupId) {
        for(var i = 0, currentGroup; !!(currentGroup = $scope._groups[i]); i++) {
          if (currentGroup !== group) {
            if (groupId === currentGroup.groupId) {
              group.error = { message: 'DUPLICATE_GROUP' };
            }
          }
        }
      }
    }

    $scope.close = function () {
      if ($scope.EDIT_GROUPS) {
        return $scope.$dismiss();
      }

      $scope.$close();

    };

    $scope.isValid = function () {
      if (actions && !actions.length) {
        return false;
      }

      for(var i = 0, group; !!(group = $scope._groups[i]); i++) {
        if (!group.groupId || group.error) {
          return false;
        }
      }

      return true;
    };

    $scope.saveChanges = function () {
      $scope.EDIT_GROUPS = false;

      doGroupUpdate(actions).then(function () {
        if (!finishedWithFailures) {
          Notifications.addMessage({
            status: messages.finished,
            message: messages.updateGroupsSuccess,
            exclusive: true
          });
        } else {
          Notifications.addError({
            status: messages.finished,
            message: messages.updateGroupsFailed,
            exclusive: true
          });
        }
      });
    };

    function doGroupUpdate (actions) {
      var deferred = $q.defer();

      var taskId = task.id;
      var count = actions.length;

      function callback(err, action) {
        if (err) {
          action.status = ACTION_STATE_FAILED;
          action.error = err;
          finishedWithFailures = true;
        }
        else {
          action.status = ACTION_STATE_SUCCESS;
        }

        count = count - 1;

        if (count === 0) {
          deferred.resolve();
        }
      }

      function performAction(action) {
        var group = action.group;
        var operation = action.operation;

        action.status = ACTION_STATE_PERFORM;

        delete group.error;

        if (operation === OPERATION_ADD) {
          Task.identityLinksAdd(taskId, group, function(err) {
            callback(err, action);
          });
        }
        else {
          Task.identityLinksDelete(taskId, group, function(err) {
            callback(err, action);
          });
        }
      }

      for (var i = 0, action; !!(action = actions[i]); i++) {
        performAction(action);
      }

      return deferred.promise;
    }

  }];

});
