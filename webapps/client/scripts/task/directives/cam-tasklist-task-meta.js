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
    '$translate',
    'camAPI',
    'Notifications',
  function(
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
        taskData: '='
      },

      template: template,

      controller: [
        '$scope',
      function(
        $scope
      ){
        
        var taskMetaData = $scope.taskData.newChild($scope);

        /**
         * observe task changes
         */
        taskMetaData.observe('task', function(task) {
          $scope.task = angular.copy(task);
        });

        /**
         * observe task changes
         */
        taskMetaData.observe('isAssignee', function(isAssignee) {
          $scope.isAssignee = isAssignee;
        });

        /**
         * reload data after the task has been updated
         */
        function reload() {

          // we always refresh the state from the backend after we made a change.
          // this has advantages:
          // - limits the risk that our copy gets corrupted
          // - we see changes made by other users faster
          taskMetaData.changed('task');

          // list of tasks must be reloaded as well:
          // changed properties on this task may cause the list to change
          taskMetaData.changed('taskList');
        }

        function saveDate(propName) {
          return function(inlineFieldScope) {
            setEditingState(propName, false);
            $scope.task[propName] = inlineFieldScope.varValue;

            updateTask();
          };
        }

        function resetProperty(propName) {
          return function() {
            $scope.task[propName] = null;
            updateTask();            
          };
        }

        function updateTask() {
          var toSend = $scope.task;

          delete toSend._embedded;
          delete toSend._links;

          Task.update(toSend, function(err, result) {
            if (err) {
              return errorNotification('TASK_UPDATE_ERROR', err);
            }

            reload();
            successNotification('TASK_UPDATE_SUCESS');
          });
        }

        function notifyOnStartEditing(property) {
          return function (inlineFieldScope) {
            setEditingState(property, true);
          };
        }

        function notifyOnCancelEditing(property) {
          return function (inlineFieldScope) {
            setEditingState(property, false);
          };
        }

        function setEditingState(property, state) {
          $scope.editingState[property] = state;
        }

        $scope.saveFollowUpDate = saveDate('followUp');
        $scope.resetFollowUpDate = resetProperty('followUp');
        $scope.startEditingFollowUpDate = notifyOnStartEditing('followUp');
        $scope.cancelEditingFollowUpDate = notifyOnCancelEditing('followUp');
        
        $scope.saveDueDate = saveDate('due');
        $scope.resetDueDate = resetProperty('due');
        $scope.startEditingDueDate = notifyOnStartEditing('due');
        $scope.cancelEditingDueDate = notifyOnCancelEditing('due');

        // initially set each control to false
        $scope.editingState = {
          followUp: false,
          due: false,
          assignee: false
        };

        $scope.now = (new Date()).toJSON();

        var notifications = {

          assigned: {
            success: 'ASSIGNED_OK',
            error: 'ASSIGNED_ERROR'
          },

          assigneeReseted: {
            success: 'ASSIGNEE_RESETED_OK',
            error: 'ASSIGNEE_RESETED_ERROR'
          },

          claimed: {
            success: 'CLAIM_OK',
            error: 'CLAIM_ERROR'
          },

          unclaimed: {
            success: 'UNCLAIM_OK',
            error: 'UNCLAIM_ERROR'
          }

        };

        $scope.startEditingAssignee = notifyOnStartEditing('assignee');
        $scope.cancelEditingAssignee = notifyOnCancelEditing('assignee');

        $scope.assign = function(inlineFieldScope) {
          setEditingState('assignee', false);

          var newAssignee = inlineFieldScope.varValue;

          if (!newAssignee) {

            if ($scope.isAssignee) {
              unclaim();
            }
            else {
              resetAssignee();
            }

          }
          else {
            setAssignee(newAssignee);
          }

        };

        var claim = $scope.claim = function() {
          var assignee = $scope.$root.authentication.name;
          Task.claim($scope.task.id, assignee, notify('claimed'));
        };

        var unclaim = $scope.unclaim = function() {
          Task.unclaim($scope.task.id, notify('unclaimed'));
        };

        var setAssignee = $scope.setAssignee = function(newAssignee) {
          Task.assignee($scope.task.id, newAssignee, notify('assigned'));
        };

        var resetAssignee = $scope.resetAssignee = function() {
          Task.assignee($scope.task.id, null, notify('assigneeReseted'));
        };

        function notify(action) {
          var messages = notifications[action];

          return function (err) {
            if (err) {
            return errorNotification(messages.error, err);
            }


          reload();
          successNotification(messages.success);

          };
        }

      }
    ]};
  }];
});
