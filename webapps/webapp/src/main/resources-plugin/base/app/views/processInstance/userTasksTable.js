/* global define: false, angular: false, require: false */
define(['angular', 'text!./identity-links-modal.html', 'text!./user-tasks-table.html'], function(angular, identityLinksTemplate, userTasksTemplate) {
  'use strict';

  return function(ngModule) {

    /**
     * @name userTaskTable
     * @memberof cam.cockpit.plugin.base.views
     * @description ---
     * @example
        TODO
     */

    /**
     * Map an array with a callback - similar to {@link http://underscorejs.org/#map|_.map()}
     *
     * @param {array} array   - the array on which iteration has to be performed
     * @param {function} cb   - the function returning the new value for each array value
     * @returns {array}       - a new array with the values produced
     */
    function map(array, cb) {
      var newArray = [];
      angular.forEach(array, function(val, key) {
        newArray[key] = cb(val, key);
      });
      return newArray;
    }

    /**
     * Removes "empty" values of an array - {@link http://underscorejs.org/#compact|_.compact()}
     *
     * @param {array} array   - the original array
     * @returns {array}       - a new array with the values produced
     */
    function compact(array) {
      var newArray = [];
      angular.forEach(array, function(val) {
        if (!!val) {
          newArray.push(val);
        }
      });
      return newArray;
    }

    /**
     * Ensure a function
     *
     * @param {?function} func - the original function
     * @returns {function}     - a function
     */
    function ensureCallback(func) {
      return angular.isFunction(func) ? func : angular.noop;
    }





    ngModule.controller('UserTaskController', [
            '$scope', 'search', 'TaskResource', 'Notifications', '$modal',
    function($scope,   search,   TaskResource,   Notifications,   $modal) {

      // input: processInstance, processData

      var userTaskData = $scope.processData.newChild($scope),
          processInstance = $scope.processInstance,
          taskIdIdToExceptionMessageMap,
          taskCopies;

      var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };

      var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

      var filter = null;

      $scope.$watch('pages.current', function(newValue, oldValue) {
        if (newValue == oldValue) {
          return;
        }

        search('page', !newValue || newValue == 1 ? null : newValue);
      });

      userTaskData.observe([ 'filter', 'executionIdToInstanceMap' ], function (newFilter, executionIdToInstanceMap) {
        pages.current = newFilter.page || 1;

        updateView(newFilter, executionIdToInstanceMap);
      });

      function updateView (newFilter, executionIdToInstanceMap) {
        filter = angular.copy(newFilter);

        delete filter.page;
        delete filter.activityIds;
        delete filter.scrollToBpmnElement;

        var page = pages.current,
            count = pages.size,
            firstResult = (page - 1) * count;

        var defaultParams = {
          processInstanceId: processInstance.id,
          processDefinitionId: processInstance.definitionId
        };

        var pagingParams = {
          firstResult: firstResult,
          maxResults: count
        };

        var params = angular.extend({}, filter, defaultParams);

        // fix missmatch -> activityInstanceIds -> activityInstanceIdIn
        params.activityInstanceIdIn = params.activityInstanceIds;
        delete params.activityInstanceIds;

        $scope.userTasks = null;

        taskIdIdToExceptionMessageMap = {};
        taskCopies = {};

        TaskResource.count(params).$promise.then(function (response) {
          pages.total = response.count;
        });

        TaskResource.query(pagingParams, params).$promise.then(function (response) {
          // for (var i = 0, task; !!(task = response.resource[i]); i++) {
          for (var i = 0, task; !!(task = response[i]); i++) {
            task.instance = executionIdToInstanceMap[task.executionId];
            taskCopies[task.id] = angular.copy(task);
          }

          // $scope.userTasks = response.resource;
          $scope.userTasks = response;
        });

      }

      $scope.getHref = function (userTask) {
        return '#/process-instance/' + processInstance.id + '?detailsTab=user-tasks-tab&activityInstanceIds=' + userTask.instance.id;
      };

      $scope.submitAssigneeChange = function(editForm, cb) {
        cb = ensureCallback(cb);

        var userTask = editForm.context;
        var copy = taskCopies[userTask.id];
        var defaultParams = {id: userTask.id};
        var params = {userId : editForm.value};

        TaskResource.setAssignee(defaultParams, params).$promise.then(
          // success
          function (response) {
            // copy.assignee = userTask.assignee = response.resource.userId;
            copy.assignee = userTask.assignee = response.userId;

            Notifications.addMessage({
              status: 'Assignee',
              message: 'The assignee of the user task \'' +
                       userTask.instance.name +
                       '\' has been set to \'' +
                       copy.assignee + '\' successfully.',
              duration: 5000
            });

            cb();
          },

          // error
          function (error) {
            var err = {
              status: 'Assignee',
              message: 'The assignee of the user task \'' +
                       userTask.instance.name +
                       '\' could not be set to \'' + copy.assignee +
                       '\' successfully.',
              exclusive: true,
              duration: 5000
            };

            Notifications.addError(err);
            taskIdIdToExceptionMessageMap[userTask.id] = error.data;
            cb(err);
          }
        );
      };

      $scope.openDialog = function(userTask, groups) {
        $modal.open({
          resolve: {
            userTask: function() { return userTask; },
            groups: function() { return groups; }
          },
          controller: 'UserTaskGroupController',
          // quick fix, should probably use the URI service...
          template: identityLinksTemplate
        });
      };

      $scope.changeGroups = function() {
        var userTask = this.userTask;

        // 1. load the identityLinks
        TaskResource.getIdentityLinks({id: userTask.id}, {}).$promise.then(function(response) {
          // 2. filter the response.data to exclude links who have no groupId or have type 'assignee' or 'owner'
          // var groups = compact(map(response.data, function(item) {
          var groups = compact(map(response, function(item) {
            var ok = item.groupId && item.type !== 'assignee' && item.type !== 'owner';
            return ok ? item : null;
          }));

          // 3. open a dialog
          $scope.openDialog(userTask, groups);
        });
      };


      $scope.getExceptionForUserTask = function (userTask) {
        return taskIdIdToExceptionMessageMap[userTask.id];
      };
    }]);

    ngModule.controller('UserTaskGroupController', [
            '$modalInstance', 'TaskResource', '$scope', 'Notifications', 'userTask', 'groups',
    function($modalInstance,   TaskResource,   $scope,   Notifications,   userTask,   groups) {
      $scope.groups = groups;

      $scope.title = 'Manage groups';

      $scope.labelKey = 'groupId';

      $scope.buttons = [
        {
          cssClass: 'btn',
          label: 'Close'
        }
      ];

      $scope.removeItem = function() {
        var delta = this.delta;
        TaskResource.deleteIdentityLink({
          id: userTask.id
        }, angular.toJson(this.group)).$promise.then(function() {
          // deleting an entry is not enough, we need to "rebuild" the groups array
          // delete $scope.groups[delta];
          $scope.groups = compact(map($scope.groups, function(g, d) {
            return delta !== d ? g : false;
          }));
        }, function(error) {
          Notifications.addError({
            status: 'Assignee',
            message: error.message,
            exclusive: true,
            duration: 5000
          });
        });
      };

      $scope.invalid = function() {
        var editForm = this.editForm;
        if (editForm.$invalid) {
          return true;
        }

        var exists;
        var newItem = editForm.newItem.$modelValue;
        angular.forEach($scope.groups, function(group) {
          exists = (exists || (group.groupId === newItem));
        });

        return exists;
      };

      $scope.addItem = function() {
        var editForm = this;


        var newGroup = {
          type: 'candidate',
          groupId: editForm.newItem
        };

        TaskResource.addIdentityLink({
          id: userTask.id
        }, newGroup).$promise.then(function() {
          $scope.groups.push(newGroup);
          editForm.newItem = '';
        }, function(error) {
          Notifications.addError({
            status: 'Assignee',
            message: error.message,
            exclusive: true,
            duration: 5000
          });
        });
      };

      $scope.close = $modalInstance.close;
    }]);

    var Configuration = function(ViewsProvider) {
      ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
        id: 'user-tasks-tab',
        label: 'User Tasks',
        template: userTasksTemplate,
        controller: 'UserTaskController',
        priority: 5
      });
    };

    Configuration.$inject = ['ViewsProvider'];

    ngModule.config(Configuration);

  };
});
