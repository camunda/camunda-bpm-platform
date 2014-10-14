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
        taskData: '=',
        tasklistData: '='
      },

      template: template,

      controller: [
        '$scope',
      function(
        $scope
      ){
        var taskData = $scope.tasklistData.newChild($scope);

        /**
         * observe task changes
         */
        taskData.observe('task', function(task) {
          $scope.task = angular.copy(task);
        });

        /**
         * reload data after the task has been updated
         */
        function reload() {

          // we always refresh the state from the backend after we made a change.
          // this has advantages:
          // - limits the risk that our copy gets corrupted
          // - we see changes made by other users faster
          taskData.changed('task');

          // list of tasks must be reloaded as well:
          // changed properties on this task may cause the list to change
          taskData.changed('taskList');
        }

        function saveDate(propName) {
          return function(inlineFieldScope) {
            var self = this;
            var toSend = self.task;


            toSend[propName] = $scope.task[propName] = inlineFieldScope.varValue;

            delete toSend._embedded;
            delete toSend._links;

            Task.update(toSend, function(err, result) {
              if (err) {
                return errorNotification('TASK_UPDATE_ERROR', err);
              }

              reload();
              successNotification('TASK_UPDATE_SUCESS');
            });
          };
        }

        $scope.saveFollowUpDate = saveDate('followUp');
        $scope.saveDueDate = saveDate('due');

        $scope.now = (new Date()).toJSON();

        function assigned(err) {
          if (err) {
            return errorNotification('ASSIGNED_ERROR', err);
          }

          reload();
          successNotification('ASSIGNED_OK');
        }


        function claimed(err) {
          if (err) {
            return errorNotification('CLAIM_ERROR', err);
          }

          reload();
          successNotification('CLAIM_OK');
        }


        function unclaimed(err) {
          if (err) {
            return errorNotification('UNCLAIM_ERROR', err);
          }

          reload();
          successNotification('UNCLAIM_OK');
        }


        $scope.userIsAssignee = function() {
          return $rootScope.authentication &&
                  $rootScope.authentication.name &&
                  $scope.task &&
                  ($rootScope.authentication.name === $scope.task.assignee);
        };


        $scope.userIsOwner = function() {
          return $rootScope.authentication &&
                  $rootScope.authentication.name &&
                  ($rootScope.authentication.name === $scope.task.owner);
        };


        $scope.validateUser = function(/*info*/) {
        };


        $scope.claim = function() {
          Task.claim($scope.task.id, $rootScope.authentication.name, claimed);
        };


        $scope.unclaim = function() {
          Task.unclaim($scope.task.id, unclaimed);
        };

        $scope.assigning = function(info) {
          if (!info.varValue) {
            return $scope.unclaim();
          }
          Task.assignee($scope.task.id, info.varValue, function(err) {
            if (err) {
              return assigned(err);
            }

            $scope.task.assignee = info.varValue;

            assigned();
          });
        };
      }
    ]};
  }];
});
