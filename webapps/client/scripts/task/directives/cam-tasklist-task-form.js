define([
  'text!./cam-tasklist-task-form.html'
], function(template) {
  'use strict';

  return [
    '$rootScope',
    'camAPI',
    'CamForm',
    '$translate',
    'Notifications',
  function(
    $rootScope,
    camAPI,
    CamForm,
    $translate,
    Notifications
  ) {
    var Task = camAPI.resource('task');


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
        task: '='
      },
      link: function(scope, element) {
        var container = element.find('.form-container');

        scope.currentTaskId = null;
        scope._camForm = null;

        function submitCb(err) {
          if (err) {
            return errorNotification('COMPLETE_ERROR', err);
          }

          scope.currentTaskId = null;
          scope._camForm = null;
          container.html('');

          $rootScope.$broadcast('tasklist.task.complete');

          successNotification('COMPLETE_OK');
        }

        scope.completeTask = function() {
          if (scope._camForm) {
            scope._camForm.submit(submitCb);
          }
          else {
            var variables = {};
            Task.submitForm({
              id: scope.currentTaskId,
              variables: variables
            }, submitCb);
          }
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

        scope.$watch('task', function(newValue, oldValue) {
          if (!scope.task) {
            scope.currentTaskId = null;
          }
          else if (newValue.id !== oldValue.id) {
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
