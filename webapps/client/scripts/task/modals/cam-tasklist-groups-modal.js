define([

], function() {
  'use strict';

  var GROUP_TYPE = 'candidate';

  return [
    '$scope',
    '$translate',
    'Notifications',
    'camAPI',
    'taskMetaData',
    'successHandler',
    'errorHandler',
  function(
    $scope,
    $translate,
    Notifications,
    camAPI,
    taskMetaData,
    successHandler,
    errorHandler
  ) {

    // setup //////////////////////////////////////////////

    var Task = camAPI.resource('task');
    var task = null;
    var groups = null;

    var taskGroupsData = taskMetaData.newChild($scope);

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    $scope.newGroup = { name: '' };

    // observe ////////////////////////////////////////////////////////

    // refresh list of groups
    taskGroupsData.changed('groups');

    $scope.modalGroupsState = taskGroupsData.observe('groups', function(_groups) {
      groups = _groups;
    });

    taskGroupsData.observe('task', function (_task) {
      task = _task;
    });

    function invalid(name) {
      if(!name || name.trim() === "") {
        return {message: 'Name can not be empty.'};
      }
      if(groups && groups.indexOf(name) !== -1) {
        return {message: 'Task is already in this group.'};
      }
    }

    $scope.addGroup = function() {
      if((err = invalid($scope.newGroup.name))) {
        return errorHandler('GROUP_ADD_ERROR', err);
      }
      Task.identityLinksAdd(task.id, {
        groupId : $scope.newGroup.name,
        type: GROUP_TYPE
      }, function(err) {
        if (err) {
          return errorHandler('GROUP_ADD_ERROR', err);
        }
        successHandler('GROUP_ADD_SUCCESS');
        $scope.newGroup.name = '';
        taskGroupsData.changed('task');
        taskGroupsData.changed('taskList');
      });
    };

    $scope.removeGroup = function(group) {
      Task.identityLinksDelete(task.id, {
        groupId : group,
        type: GROUP_TYPE
      }, function(err) {
        if (err) {
          return errorHandler('GROUP_DELETE_ERROR', err);
        }
        successHandler('GROUP_DELETE_SUCCESS');
        taskGroupsData.changed('task');
        taskGroupsData.changed('taskList');
      });
    };
  }];

});
