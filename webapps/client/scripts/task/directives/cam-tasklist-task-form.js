define([
  'text!./cam-tasklist-task-form.html',
  'text!./cam-tasklist-task-form-modal.html',
  'angular'
], function(
  template,
  modalTemplate,
  angular
) {
  'use strict';
  var $ = angular.element;
  var each = angular.forEach;

  return [
    'camAPI',
    'CamForm',
    '$translate',
    'Notifications',
    '$location',
  function(
    camAPI,
    CamForm,
    $translate,
    Notifications,
    $location
  ) {
    var Task = camAPI.resource('task');
    var ProcessDefinition = camAPI.resource('process-definition');

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

    return {
      scope: {
        taskData: '='
      },

      link: function(scope, element) {

        var taskFormData = scope.taskFormData = scope.taskData.newChild(scope);

        var container = element.find('.form-container');
        var modalInstance;
        scope._camForm = null;
        scope.task = {};

        taskFormData.observe('task', function (task) {
          scope.task = task;
          loadForm();
        });

        taskFormData.observe('isAssignee', function(isAssignee) {
          scope.isAssignee = isAssignee;
        });

        function submitCb(err) {
          if (err) {
            return errorNotification('COMPLETE_ERROR', err);
          }

          scope._camForm = null;
          container.html('');

          successNotification('COMPLETE_OK');

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

        scope.completeTask = function() {
          if (scope._camForm) {
            scope._camForm.submit(submitCb);
          }
          else {
            var variables = {};
            Task.submitForm({
              id: scope.task.id,
              variables: variables
            }, submitCb);
          }
        };

        function showForm(targetContainer, processDefinition) {
          var parts = (scope.task.formKey || '').split('embedded:');
          var ctx = processDefinition.contextPath;
          var formUrl;

          if (parts.length > 1) {
            formUrl = parts.pop();
            // ensure a trailing slash
            ctx = ctx + (ctx.slice(-1) !== '/' ? '/' : '');
            formUrl = formUrl.replace(/app:(\/?)/, ctx);
          }
          else {
            formUrl = scope.task.formKey;
          }

          if (formUrl) {
            scope._camForm = new CamForm({
              taskId:           scope.task.id,
              containerElement: targetContainer,
              client:           camAPI,
              formUrl:          formUrl,
              initialized:      function(camForm) {

                var formName = camForm.formElement.attr('name');

                var camFormScope = camForm.formElement.scope();
                if (!camFormScope) { return; }

                var form = camFormScope[formName];

                scope.$watch(function() {
                  return form.$valid && scope.isAssignee;
                }, function(value) {
                  scope.$invalid = !value;
                });
              }
            });

          }
          else {
            scope.$invalid = false;

            // clear the content (to avoid other tasks form to appear)
            $translate('NO_TASK_FORM').then(function(translated) {
              targetContainer.html(translated || '');
            });
          }
        }

        function loadForm(targetContainer) {

          targetContainer = targetContainer || container;
          scope._camForm = null;

          if (scope.task._embedded && scope.task._embedded.processDefinition[0]) {
            showForm(targetContainer, scope.task._embedded.processDefinition[0]);
          }
          else {
            // this should not happen, but...
            ProcessDefinition.get(scope.task.processDefinitionId, function(err, result) {
              if (err) {
                return errorNotification('TASK_NO_PROCESS_DEFINITION', err);
              }
              scope.task._embedded.processDefinition = scope.task._embedded.processDefinition || [];
              scope.task._embedded.processDefinition[0] = result;

              showForm(targetContainer, scope.task._embedded.processDefinition[0]);
            });
          }
        }

      },
      template: template
    };
  }];
});
