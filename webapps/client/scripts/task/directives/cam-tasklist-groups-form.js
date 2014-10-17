define([

], function() {
  'use strict';

  return [
    '$scope',
    '$translate',
    'Notifications',
    'camAPI',
    'task',
    'groups',
    'taskMetaData',
  function(
    $scope,
    $translate,
    Notifications,
    camAPI,
    task,
    groups,
    taskMetaData
  ) {

    var Task = camAPI.resource('task');

    var taskGroupsData = taskMetaData.newChild($scope);

    $scope.newGroup = {name: ''};

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    function errorNotification(src, err) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : ''),
          exclusive: true
        });
      });
    }

    function successNotification(src) {
      $translate(src).then(function(translated) {
        Notifications.addMessage({
          duration: 3000,
          status: translated
        });
      });
    }

    function invalid(name) {
      if(name.trim() === "") {
        return {message: 'Name can not be empty.'};
      }
      if(groups.indexOf(name) !== -1) {
        return {message: 'Task is already in this group.'};
      }
    }

    $scope.addGroup = function() {
      if((err = invalid($scope.newGroup.name))) {
        return errorNotification('GROUP_ADD_ERROR', err);
      }
      Task.identityLinksAdd(task.id, {
        groupId : $scope.newGroup.name,
        type: 'candidate'
      }, function(err) {
        if (err) {
          return errorNotification('GROUP_ADD_ERROR', err);
        }
        successNotification('GROUP_ADD_SUCCESS');
        $scope.newGroup.name = '';
        taskGroupsData.changed('task');
        taskGroupsData.changed('taskList');
      });
    };

    $scope.removeGroup = function(group) {
      Task.identityLinksDelete(task.id, {
        groupId : group,
        type: 'candidate'
      }, function(err) {
        if (err) {
          return errorNotification('GROUP_DELETE_ERROR', err);
        }
        successNotification('GROUP_DELETE_SUCCESS');
        taskGroupsData.changed('task');
        taskGroupsData.changed('taskList');
      });
    };
  }];

});
