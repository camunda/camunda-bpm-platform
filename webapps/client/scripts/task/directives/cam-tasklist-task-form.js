define([
  'text!./cam-tasklist-task-form.html'
], function(template) {
  'use strict';

  return [
    'camAPI',
    'CamForm',
    '$translate',
    'Notifications',
  function(
    camAPI,
    CamForm,
    $translate,
    Notifications
  ) {
    // var Task = camAPI.resource('task');

    return {
      scope: {
        task: '='
      },
      link: function(scope, element) {
        var container = element.find('.form-container');
        scope.currentTaskId = null;
        scope._camForm = null;

        scope.completeTask = function() {
          scope._camForm.submit(function(err, resp) {
            if (err) {
              $translate('COMPLETE_ERROR').then(function(translated) {
                Notifications.addError({
                  status: translated,
                  message: err.message
                });
              });
            }

            console.info('task submit response', resp);
            // TODO: reload the tasks list
            $translate('COMPLETE_OK').then(function(translated) {
              Notifications.addMessage({
                duration: 3000,
                status: translated
              });
            });
          });
        };

        function loadForm() {
          scope._camForm = null;

          var parts = (scope.task.formKey || '').split('embedded:');
          var ctx = scope.task._embedded.processDefinition.contextPath;
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
              containerElement: container,
              client:           camAPI,
              formUrl:          formUrl
            });
          }
          else {
            // clear the content (to avoid other tasks form to appear)
            $translate('NO_TASK_FORM').then(function(translated) {
              container.html(translated || '');
            });
          }
        }

        scope.$watch('task', function() {
          if (!scope.task) {
            scope.currentTaskId = null;
          }
          else if (scope.currentTaskId !== scope.task.id) {
            scope.currentTaskId = scope.task.id;

            loadForm();
          }
        });

        if (scope.task) {
          scope.currentTaskId = scope.task.id;

          loadForm();
        }
      },
      template: template
    };
  }];
});
