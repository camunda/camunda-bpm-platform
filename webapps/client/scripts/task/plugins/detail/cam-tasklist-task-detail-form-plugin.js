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
  function (
    $scope,
    $location,
    camAPI
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

    function checkForAssignedTasks() {
      Task.list({
        processInstanceId : $scope.task.processInstanceId,
        assignee : $scope.task.assignee
      },function(err, data) {
        if(data._embedded.task.length > 0) {
          var msg = "";
          for(var task, i = 0; !!(task = data._embedded.task[i]); i++) {
            msg += '<a ng-href="#/?task='+ task.id +'" ng-click="removeNotification(notification)">'+task.name+'</a>, ';
          }
          successHandler("You are assigned to the following tasks in the same process", msg.slice(0,-2), 16000);
        }
      });
    }

    // will be called when the form has been submitted
    $scope.completionCallback = function(err) {
      if (err) {
        return errorHandler('COMPLETE_ERROR', err);
      }
      successHandler('COMPLETE_OK');
      checkForAssignedTasks();
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
