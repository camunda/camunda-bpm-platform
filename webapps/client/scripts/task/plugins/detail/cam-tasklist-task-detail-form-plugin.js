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
   '$translate',
   'Notifications',
  function (
    $scope,
    $location,
    $translate,
    Notifications
  ) {

    function errorNotification(src, err) {
      $translate(src).then(function(translated) {
        Notifications.addError({
          status: translated,
          message: (err ? err.message : '')
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

    // setup ///////////////////////////////////////////////////////////

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

    function enhanceErrorMessage(err) {
      if(err.indexOf("task is null") !== -1) {
        // task does not exist (e.g. completed by someone else)
        return "TASK_NOT_EXIST";
      }
      if(err.indexOf("is suspended") !== -1) {
        // process instance is suspended
        return "INSTANCE_SUSPENDED";
      }
      return err;
    }

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
        var errorMsg = enhanceErrorMessage(err.message);

        return $translate(errorMsg).then(function(translated) {
          err.message = translated;

          errorNotification('COMPLETE_ERROR', err);

          if(errorMsg === "TASK_NOT_EXIST" || errorMsg === "INSTANCE_SUSPENDED") {
            clearTask();
          }

        });
      }

      successNotification('COMPLETE_OK');
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
