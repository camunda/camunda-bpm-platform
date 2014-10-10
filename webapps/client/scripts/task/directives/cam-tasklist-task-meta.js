define([
  'angular',
  'text!./cam-tasklist-task-meta.html'
], function(
  angular,
  template
) {
  'use strict';

  var $ = angular.element;

  return [
    '$rootScope',
    '$translate',
    'camAPI',
    'Notifications',
  function(
    $rootScope,
    $translate,
    camAPI,
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

      template: template,

      link: function(scope, element) {
        function saveDate(propName) {
          return function(inlineFieldScope) {
            var self = this;
            var toSend = angular.copy(self.task);

            toSend[propName] = scope.task[propName] = inlineFieldScope.varValue;

            delete toSend._embedded;
            delete toSend._links;

            Task.update(toSend, function(err, result) {
              if (err) {
                return errorNotification('TASK_UPDATE_ERROR', err);
              }

              $rootScope.$broadcast('tasklist.task.update');
              $rootScope.$broadcast('tasklist.task.'+propName);

              successNotification('TASK_UPDATE_SUCESS');
            });
          };
        }

        scope.saveFollowUpDate = saveDate('followUp');
        scope.saveDueDate = saveDate('due');

        scope.now = (new Date()).toJSON();

        function assigned(err) {
          if (err) {
            return errorNotification('ASSIGNED_ERROR', err);
          }

          successNotification('ASSIGNED_OK');
          $rootScope.$broadcast('tasklist.task.update');
          $rootScope.$broadcast('tasklist.task.assign');
        }


        function claimed(err) {
          if (err) {
            return errorNotification('CLAIM_ERROR', err);
          }

          scope.task.assignee = $rootScope.authentication.name;

          successNotification('CLAIM_OK');
          $rootScope.$broadcast('tasklist.task.update');
          $rootScope.$broadcast('tasklist.task.claim');
        }


        function unclaimed(err) {
          if (err) {
            return errorNotification('UNCLAIM_ERROR', err);
          }

          scope.task.assignee = null;

          successNotification('UNCLAIM_OK');
          $rootScope.$broadcast('tasklist.task.update');
          $rootScope.$broadcast('tasklist.task.unclaim');
        }


        scope.userIsAssignee = function() {
          return $rootScope.authentication &&
                  $rootScope.authentication.name &&
                  ($rootScope.authentication.name === scope.task.assignee);
        };


        scope.userIsOwner = function() {
          return $rootScope.authentication &&
                  $rootScope.authentication.name &&
                  ($rootScope.authentication.name === scope.task.owner);
        };


        scope.validateUser = function(/*info*/) {
        };


        scope.claim = function() {
          Task.claim(scope.task.id, $rootScope.authentication.name, claimed);
        };


        scope.unclaim = function() {
          Task.unclaim(scope.task.id, unclaimed);
        };

        scope.assigning = function(info) {
          if (!info.varValue) {
            return scope.unclaim();
          }
          Task.assignee(scope.task.id, info.varValue, function(err) {
            if (err) {
              return assigned(err);
            }

            scope.task.assignee = info.varValue;

            assigned();
          });
        };


        scope.$on('tasklist.task.current', function(evt, task) {
          $('[cam-form-inline-field]', element).each(function() {
            $(this)
              .isolateScope()
              .cancelChange();
          });
        });
      }
    };
  }];
});
