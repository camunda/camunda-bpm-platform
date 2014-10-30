define([
  'angular',
  'text!./cam-tasklist-task-detail-form-plugin.html',
], function(
  angular,
  template
) {
  'use strict';

  var Controller = [
   '$scope',
   '$location',
   'camAPI',
   'assignNotification',
  function (
    $scope,
    $location,
    camAPI,
    assignNotification
  ) {

    // setup ///////////////////////////////////////////////////////////

    var errorHandler = $scope.errorHandler;
    var successHandler = $scope.successHandler;
    var Task = camAPI.resource('task');

    var DEFAULT_OPTIONS = $scope.options = {
      hideCompleteButton: false,
      disableCompleteButton: false,
      disableForm: false,
      disableAddVariableButton: false
    };

    var taskFormData = $scope.taskData.newChild($scope);

    // observer ///////////////////////////////////////////////////////////

    taskFormData.observe(['task', 'isAssignee', function(task, isAssignee) {
      if (task && task.id) {
        $scope.options = angular.extend({}, { taskId : task.id }, DEFAULT_OPTIONS);
      }
      else {
        $scope.options = DEFAULT_OPTIONS;
      }

      $scope.options.disableCompleteButton = !isAssignee;
      $scope.options.disableForm = !isAssignee;
      $scope.options.disableAddVariableButton = !isAssignee;
    }]);

    $scope.taskFormState = taskFormData.observe('taskForm', function(taskForm) {
      $scope.taskForm = angular.copy(taskForm);
    });

    // task form /////////////////////////////////////////////////////////////////////////

    function clearTask() {
      // reseting the location leads that
      // the taskId will set to null and
      // the current selected task will
      // also be set to null, so that the
      // view gets clear
      $location.search({});

      // list of tasks must be reloaded as
      // well: changed properties on this
      // task may cause the list to change
      taskFormData.changed('taskList');
    }

    // will be called when the form has been submitted
    $scope.completionCallback = function(err) {
      if (err) {
        return errorHandler('COMPLETE_ERROR', err);
      }
      successHandler('COMPLETE_OK');
      if($scope.task.processInstanceId) {
        assignNotification({
          assignee: $scope.task.assignee,
          processInstanceId: $scope.task.processInstanceId
        });
      } else if($scope.task.caseInstanceId) {
        assignNotification({
          assignee: $scope.task.assignee,
          caseInstanceId: $scope.task.caseInstanceId
        });
      }

      clearTask();
    };
  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('tasklist.task.detail', {
      id: 'task-detail-form',
      label: 'FORM',
      template: template,
      controller: Controller,
      priority: 1000
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
