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
    '$rootScope',
    'camAPI',
    'CamForm',
    '$translate',
    'Notifications',
    '$interval',
    '$modal',
  function(
    $rootScope,
    camAPI,
    CamForm,
    $translate,
    Notifications,
    $interval,
    $modal
  ) {
    var Task = camAPI.resource('task');
    var ProcessDefinition = camAPI.resource('process-definition');

    function setModalFormMaxHeight(wrapper, targetContainer) {
      var availableHeight = $(window).height();

      wrapper.find('> div').each(function() {
        var $el = $(this);
        if ($el.hasClass('form-container')) { return; }
        availableHeight -= $el.outerHeight();
      });

      targetContainer.css({
        'overflow': 'auto',
        'max-height': availableHeight +'px'
      });
    }

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
        var modalInstance;

        scope.currentTaskId = null;
        scope._camForm = null;
        scope.fullscreen = false;

        scope.enterFullscreen = function() {
          scope.fullscreen = !scope.fullscreen;

          modalInstance = $modal.open({
            // by passing the scope, we also pass the methods to submit the form
            scope:        scope,
            template:     modalTemplate,
            windowClass:  'task-form-fullscreen'
          });

          modalInstance.opened.then(function() {
            // dat ain't neat... dat is di only way to make sure da containa is inna di place
            var targetContainer;
            var checkContainerInterval = $interval(function() {
              targetContainer = $('.modal-content .form-container');
              if (targetContainer.length) {
                targetContainer.html('').append(scope._camForm.formElement);

                var wrapper = $('.modal-content');
                setModalFormMaxHeight(wrapper, targetContainer);
                $(window).on('resize', function() {
                  setModalFormMaxHeight(wrapper, targetContainer);
                });

                // fatha always said: don't work for free
                $interval.cancel(checkContainerInterval);
              }
            }, 100, 20, false);
          });
        };

        scope.exitFullscreen = function() {
          scope.fullscreen = !scope.fullscreen;
          container.html('').append(scope._camForm.formElement);

          if (modalInstance) {
            try {
              modalInstance.dismiss();
            } catch (e) {}
            $(window).off('resize');
          }
        };

        scope.toggleFullscreen = function() {
          if (!scope.fullscreen) {
            scope.enterFullscreen();
          }
          else {
            scope.exitFullscreen();
          }
        };



        function submitCb(err) {
          if (err) {
            return errorNotification('COMPLETE_ERROR', err);
          }

          scope.currentTaskId = null;
          scope._camForm = null;
          container.html('');

          if (modalInstance) {
            try {
              modalInstance.dismiss();
            } catch (e) {}
          }

          $rootScope.$broadcast('tasklist.task.update');
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
          if (!scope.$root.authentication) {
            throw new Error('Not authenticated');
          }

          targetContainer = targetContainer || container;
          scope._camForm = null;

          scope.isAssignee = scope.task.assignee &&
                             scope.$root.authentication &&
                             scope.task.assignee === scope.$root.authentication.name;

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
