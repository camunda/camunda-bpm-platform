define([
  'text!./cam-tasklist-task-meta.html'
], function(template) {
  'use strict';

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

      link: function(scope) {




        function saveDate(propName) {
          return function(inlineFieldScope) {
            var self = this;
            var task = angular.copy(self.task);
            task[propName] = inlineFieldScope.varValue;

            delete task._embedded;
            delete task._links;

            Task.update(task, function(err, result) {
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



        // function delegated(err) {
        //   if (err) {
        //     return errorNotification('DELEGATE_ERROR', err);
        //   }

        //   successNotification('DELEGATE_OK');
        // }


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
          // console.info('validateUser', this, this === scope, info);
        };


        scope.claim = function() {
          // if (scope.userIsOwner()) {
          //   Task.assignee(scope.task.id, $rootScope.authentication.name, function(err) {
          //     if (err) {
          //       return claimed(err);
          //     }

          //     scope.task.owner = null;
          //     scope.task.assignee = $rootScope.authentication.name;

          //     claimed();
          //   });
          // }
          // else {
          //   Task.claim(scope.task.id, $rootScope.authentication.name, claimed);
          // }

          Task.claim(scope.task.id, $rootScope.authentication.name, claimed);
        };


        scope.unclaim = function() {
          Task.unclaim(scope.task.id, unclaimed);
        };


        // scope.delegate = function() {
        //   delegated(null, {userId: $rootScope.authentication.name});
        // };


        // scope.assigning = function(info) {
        //   // delegation
        //   if (scope.userIsAssignee()) {
        //     Task.delegate(scope.task.id, info.varValue, function(err) {
        //       if (err) {
        //         return delegated(err);
        //       }

        //       // scope.task.owner = $rootScope.authentication.name;
        //       scope.task.assignee = info.varValue;

        //       delegated();
        //     });
        //   }
        //   // assignment
        //   else if (scope.userIsOwner()) {
        //     Task.assignee(scope.task.id, info.varValue, function(err) {
        //       if (err) {
        //         return assigned(err);
        //       }

        //       scope.task.assignee = info.varValue;

        //       assigned();
        //     });
        //   }
        //   else {
        //     errorNotification('NEED_OWNER_OR_ASSIGNEE');
        //   }
        // };

        scope.assigning = function(info) {
          Task.assignee(scope.task.id, info.varValue, function(err) {
            if (err) {
              return assigned(err);
            }

            scope.task.assignee = info.varValue;

            assigned();
          });
        };



        // function resolved(err/*, resp*/) {
        //   if (err) {
        //     return errorNotification('RESOLVE_ERROR', err);
        //   }

        //   scope.task.assignee = null;

        //   successNotification('RESOLVE_OK');
        // }
        // scope.resolve = function() {
        //   resolved(null, {userId: $rootScope.authentication.name});
        //   // Task.resolve($rootScope.authentication.name, {
        //   //   done: assigned
        //   // });
        // };
      }
    };
  }];
});
