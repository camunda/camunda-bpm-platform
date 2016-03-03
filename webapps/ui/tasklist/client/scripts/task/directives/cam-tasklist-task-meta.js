'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-task-meta.html', 'utf8');
var editGroupsFormTemplate = fs.readFileSync(__dirname + '/../modals/cam-tasklist-groups-modal.html', 'utf8');

var angular = require('camunda-bpm-sdk-js/vendor/angular');

  module.exports = [
    '$modal',
    'camAPI',
  function(
    $modal,
    camAPI
  ) {
    var Task = camAPI.resource('task');

    return {
      scope: {
        taskData: '=',
        successHandler: '&',
        errorHandler: '&'
      },

      template: template,

      controller: [
        '$scope',
      function(
        $scope
      ){

        var taskMetaData = $scope.taskData.newChild($scope);

        var successHandler = $scope.successHandler() || function () {};
        var errorHandler = $scope.errorHandler() || function () {};

        /**
         * observe task changes
         */
        taskMetaData.observe('task', function(task) {
          $scope.task = angular.copy(task);
        });

        taskMetaData.observe('assignee', function(assignee) {
          $scope.assignee = angular.copy(assignee);
        });


        /**
         * observe task changes
         */
        taskMetaData.observe('isAssignee', function(isAssignee) {
          $scope.isAssignee = isAssignee;
        });

        taskMetaData.observe('groups', function(groups) {
          groups = groups || [];
          var groupNames = [];
          for (var i = 0, group; !!(group = groups[i]); i++) {
            groupNames.push(group.name || group.id);
          }
          $scope.groupNames = groupNames;
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
            reload();
            if (err) {
              return errorHandler('TASK_UPDATE_ERROR', err);
            }
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

        $scope.openDatepicker = function(evt) {
          if(evt.keyCode === 13 && evt.target === evt.currentTarget) {
            // we can not trigger events in an event handler, because 'apply is already in progress' ;)
            window.setTimeout(function(){

              // activate the inline edit field
              evt.target.firstChild.click();

              // wait for angular to open the date picker
              window.setTimeout(function() {

                // wait for the update of the inline edit field, otherwise it will steal out focus
                window.setTimeout(function() {

                  // set the focus to the date picker
                  document.querySelector('.cam-widget-inline-field.field-control > .datepicker > table').focus();
                });
              });
            });
          }
        };

        $scope.editAssignee = function(evt) {
          if(evt.keyCode === 13 && evt.target === evt.currentTarget) {
            // we can not trigger events in an event handler, because 'apply is already in progress' ;)
            window.setTimeout(function(){
              evt.target.firstChild.click();
            });
          }
        };

        var notifications = {

          assigned: {
            error: 'ASSIGNED_ERROR'
          },

          assigneeReseted: {
            error: 'ASSIGNEE_RESET_ERROR'
          },

          claimed: {
            error: 'CLAIM_ERROR'
          },

          unclaimed: {
            error: 'UNCLAIM_ERROR'
          }

        };

        $scope.startEditingAssignee = notifyOnStartEditing('assignee');
        $scope.cancelEditingAssignee = notifyOnCancelEditing('assignee');

        $scope.assign = function(inlineFieldScope) {
          setEditingState('assignee', false);

          var newAssignee = inlineFieldScope.varValue.trim();

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

        $scope.editGroups = function() {
          var groupsChanged;

          $modal.open({
            // creates a child scope of a provided scope
            scope: $scope,
            //TODO: extract filter edit modal class to super style sheet
            windowClass: 'filter-edit-modal',
            // size: 'md',
            template: editGroupsFormTemplate,
            controller: 'camGroupEditModalCtrl',
            resolve: {
              taskMetaData: function() { return taskMetaData; },
              groupsChanged: function() {
                return function () {
                  groupsChanged = true;
                };
              },
              errorHandler: function () { return $scope.errorHandler; }
            }
          }).result.then(dialogClosed, dialogClosed);

          function dialogClosed() {
            if (groupsChanged) {
              taskMetaData.set('taskId', { taskId: $scope.task.id });
              taskMetaData.changed('taskList');
            }
          }

        };

        function notify(action) {
          var messages = notifications[action];

          return function (err) {
            if (err) {
              return errorHandler(messages.error, err);
            }

            reload();
          };
        }

      }
    ]};
  }];
