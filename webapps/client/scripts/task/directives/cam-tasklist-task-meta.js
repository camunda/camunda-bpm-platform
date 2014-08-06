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
          message: translated +' '+ (err ? err.message : '')
        });
      });
    }

    function successNotification(src) {
      $translate(src).then(function(translated) {
        Notifications.addMessage({
          duration: 3000,
          message: translated
        });
      });
    }

    return {
      scope: {
        task: '='
      },
      link: function(scope) {

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
        }


        function claimed(err) {
          if (err) {
            return errorNotification('CLAIM_ERROR', err);
          }

          scope.task.assignee = $rootScope.authentication.name;

          successNotification('CLAIM_OK');
        }


        function unclaimed(err) {
          if (err) {
            return errorNotification('UNCLAIM_ERROR', err);
          }

          scope.task.assignee = null;

          successNotification('UNCLAIM_OK');
        }


        scope.userIsAssignee = function() {
          return $rootScope.authentication.name &&
                 ($rootScope.authentication.name === scope.task.assignee);
        };


        scope.userIsOwner = function() {
          return $rootScope.authentication.name &&
                 ($rootScope.authentication.name === scope.task.owner);
        };


        scope.validateUser = function(/*info*/) {
          // console.info('validateUser', this, this === scope, info);
        };


        scope.claim = function() {
          if (scope.userIsOwner()) {
            Task.assignee(scope.task.id, $rootScope.authentication.name, function(err) {
              if (err) {
                return claimed(err);
              }

              scope.task.owner = null;
              scope.task.assignee = $rootScope.authentication.name;

              claimed();
            });
          }
          else {
            Task.claim(scope.task.id, $rootScope.authentication.name, claimed);
          }
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
      },
      template: template
    };
  }];
});
