'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-task-meta.html', 'utf8');
var editGroupsFormTemplate = fs.readFileSync(__dirname + '/../modals/cam-tasklist-groups-modal.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$modal',
  '$timeout',
  'camAPI',
  function(
    $modal,
    $timeout,
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
      link: function($scope, $element) {
        var taskMetaData = $scope.taskData.newChild($scope);

        $scope.successHandler() || function() {};
        var errorHandler = $scope.errorHandler() || function() {};

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
          for (var i = 0, group; (group = groups[i]); i++) {
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

            document.querySelector('[cam-widget-inline-field].'+(propName.toLowerCase())+'-date').focus();
          };
        }

        function resetProperty(propName) {
          return function() {
            $scope.task[propName] = null;
            updateTask();

            document.querySelector('[cam-widget-inline-field].'+(propName.toLowerCase())+'-date').focus();
          };
        }

        function updateTask() {
          var toSend = $scope.task;

          delete toSend._embedded;
          delete toSend._links;

          Task.update(toSend, function(err) {
            reload();
            if (err) {
              return errorHandler('TASK_UPDATE_ERROR', err);
            }
          });
        }

        function focusAssignee() {
          var el = document.querySelector('[cam-tasklist-task-meta] [cam-widget-inline-field][value="assignee.id"]');
          if(el) {
            el.focus();
          } else {
            el = document.querySelector('[cam-tasklist-task-meta] .claim');
            if(el) {
              el.focus();
            }
          }
        }

        function notifyOnStartEditing(property) {
          return function() {
            if (property === 'assignee') {
              return validateAssignee($scope.assignee.id, function() {
                setEditingState(property, true);
              });
            }
            setEditingState(property, true);
          };
        }

        function notifyOnCancelEditing(property) {
          return function() {
            var el;
            setEditingState(property, false);
            if(property === 'assignee') {
              el = document.querySelector('[cam-tasklist-task-meta] [cam-widget-inline-field][value="assignee.id"]');
            } else {
              el = document.querySelector('[cam-widget-inline-field].'+(property.toLowerCase())+'-date');
            }
            if(el) {
              el.focus();
            }
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
            $timeout(function() {

              // activate the inline edit field
              evt.target.firstChild.click();
            });
          }
        };

        // ------------ START VALIDATION LOGIC --------------

        var userResource = camAPI.resource('user');
        $scope.validAssignee = true; // we assume it's valid, good idea?
        $scope.validationInProgress = false; // not yet started the validation

        var previousAssigneeInput;
        function validateAssignee(targetElement, done) {
          done = done || angular.noop;
          var newId = targetElement.value;
          if ($scope.validationInProgress || previousAssigneeInput === newId) {
            return done();
          }

          if (!newId) {
            $scope.validAssignee = true; // dunno... should it be false?
            $scope.validationInProgress = false;
            return done();
          }

          previousAssigneeInput = newId;
          $scope.validAssignee = false;
          $scope.validationInProgress = true;

          userResource.list({
            maxResults: 1, // we don't do suggestions, yet
            id: newId
          }, function(err, results) {
            if (newId !== targetElement.value) {
              $scope.validationInProgress = false;
              return validateAssignee(targetElement, done);
            }

            $scope.validAssignee = !err && results.length;
            $scope.validationInProgress = false;

            done();
          });
        }

        // this is used by the inline-field widget to allow or reject the change
        $scope.isInvalidUser = function() {
          // must wait for 'validationInProgress' to be back to 'false'
          return $scope.validationInProgress || !$scope.validAssignee;
        };

        // used on keydown
        $scope.editAssignee = function(evt) {
          $timeout(function() {
            validateAssignee(evt.target, function() {
              if(evt.keyCode === 13 && evt.target === evt.currentTarget) {
                evt.target.firstChild.click();
              }
            });
          });
        };

        // ------------ END VALIDATION LOGIC --------------

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
          var original = $scope.assignee ? $scope.assignee.id : '';

          validateAssignee($element.find('.assignee input')[0], function() {
            if (!$scope.validAssignee) {
              inlineFieldScope.varValue = original;
              $scope.validAssignee = true;
              return;
            }
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
          });
        };

        var claim = $scope.claim = function() {
          var assignee = $scope.$root.authentication.name;
          Task.claim($scope.task.id, assignee, function(err) {
            doAfterAssigneeLoaded.push(focusAssignee);
            notify('claimed')(err);
          });
        };
        $scope.$on('shortcut:claimTask', claim);

        var unclaim = $scope.unclaim = function() {
          Task.unclaim($scope.task.id, function(err) {
            doAfterAssigneeLoaded.push(focusAssignee);
            notify('unclaimed')(err);
          } );
        };

        var setAssignee = $scope.setAssignee = function(newAssignee) {
          Task.assignee($scope.task.id, newAssignee, function(err) {
            doAfterAssigneeLoaded.push(focusAssignee);
            notify('assigned')(err);
          });
        };

        var resetAssignee = $scope.resetAssignee = function() {
          Task.assignee($scope.task.id, null, function(err) {
            doAfterAssigneeLoaded.push(focusAssignee);
            notify('assigneeReseted')(err);
          });
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
                return function() {
                  groupsChanged = true;
                };
              },
              errorHandler: function() { return $scope.errorHandler; }
            }
          }).result.then(dialogClosed, dialogClosed);

          function dialogClosed() {
            if (groupsChanged) {
              taskMetaData.set('taskId', { taskId: $scope.task.id });
              taskMetaData.changed('taskList');

            // okay, here is where it gets ugly: since the groups have changed, a listener to the event we just fired
            // will update the task. that means that the complete html of the task is going to be replaced at some point in the future
            // after this replacement, we have to set the focus to the groups trigger again
              doAfterGroupsLoaded.push(function() {
                $timeout(function() {
                  document.querySelector('.meta .groups a').focus();
                });
              });

            } else {
              document.querySelector('.meta .groups a').focus();
            }
          }

        };

        var doAfterGroupsLoaded = [];
        $scope.$watch('groupNames', function() {
          doAfterGroupsLoaded.forEach(function(fct) {
            fct();
          });
          doAfterGroupsLoaded = [];
        });

        var doAfterAssigneeLoaded = [];
        $scope.$watch('assignee', function() {
          doAfterAssigneeLoaded.forEach(function(fct) {
            $timeout(fct);
          });
          doAfterAssigneeLoaded = [];
        });


        function notify(action) {
          var messages = notifications[action];

          return function(err) {
            if (err) {
              return errorHandler(messages.error, err);
            }

            reload();
          };
        }

      }
    };
  }];
