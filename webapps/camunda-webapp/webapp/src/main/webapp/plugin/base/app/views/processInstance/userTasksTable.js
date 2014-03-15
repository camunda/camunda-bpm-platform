/* global ngDefine: false, angular: false, console: false */

ngDefine('cockpit.plugin.base.views', function(module) {
  'use strict';
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

  function UserTaskController ($scope, search, TaskResource, Notifications, $dialog) {

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

      TaskResource.count(params).$then(function (response) {
        pages.total = Math.ceil(response.data.count / pages.size);
      });

      TaskResource.query(pagingParams, params).$then(function (response) {
        for (var i = 0, task; !!(task = response.resource[i]); i++) {
          task.instance = executionIdToInstanceMap[task.executionId];
          taskCopies[task.id] = angular.copy(task);
        }

        $scope.userTasks = response.resource;
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

      TaskResource.setAssignee(defaultParams, params).$then(
        // success
        function (response) {
          copy.assignee = userTask.assignee = response.resource.userId;

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
      var dialog = $dialog.dialog({
        resolve: {
          userTask: function() { return userTask; },
          groups: function() { return groups; }
        },
        controller: 'UserTaskGroupController',
        templateUrl: require.toUrl('./plugin/base/app/views/processInstance/identity-links-modal.html')
      });

      dialog.open();
    };

    $scope.changeGroups = function() {
      var userTask = this.userTask;

      // 1. load the identityLinks
      TaskResource.getIdentityLinks({id: userTask.id}, {}).$then(function(response) {
        // 2. filter the response.data to exclude links who have no groupId or have type 'assignee' or 'owner'
        var groups = compact(map(response.data, function(item) {
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
  }

  function UserTaskGroupController (dialog, TaskResource) {
    var dialogScope = dialog.$scope;

    // fill the dialogScope with the resolved references
    angular.forEach(dialog.options.resolve, function(func, name) {
      dialogScope[name] = func();
    });

    dialogScope.title = 'Manage groups';

    dialogScope.labelKey = 'groupId';

    dialogScope.buttons = [
      {
        cssClass: 'btn',
        label: 'Close'
      }
    ];

    dialogScope.removeItem = function() {
      var delta = this.delta;
      TaskResource.deleteIdentityLink({
        id: dialogScope.userTask.id
      }, angular.toJson(this.group)).$then(function() {
        // deleting an entry is not enough, we need to "rebuild" the groups array
        // delete dialogScope.groups[delta];
        dialogScope.groups = compact(map(dialogScope.groups, function(g, d) {
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

    dialogScope.invalid = function() {
      var editForm = this.editForm;
      if (editForm.$invalid) {
        return true;
      }

      var exists;
      var newItem = editForm.newItem.$modelValue;
      angular.forEach(dialogScope.groups, function(group) {
        exists = (exists || (group.groupId === newItem));
      });

      return exists;
    };

    dialogScope.addItem = function() {
      var editForm = this;

      var newGroup = {
        type: 'candidate',
        groupId: dialogScope.newItem
      };

      TaskResource.addIdentityLink({
        id: dialogScope.userTask.id
      }, newGroup).$then(function() {
        dialogScope.groups.push(newGroup);
        dialogScope.newItem = '';
      }, function(error) {
        Notifications.addError({
          status: 'Assignee',
          message: error.message,
          exclusive: true,
          duration: 5000
        });
      });
    };

    dialogScope.close = function(res){
      dialog.close(res);
    };
  }

  module.controller('UserTaskController', [
    '$scope',
    'search',
    'TaskResource',
    'Notifications',
    '$dialog',
  UserTaskController]);

  module.controller('UserTaskGroupController', [
    'dialog',
    'TaskResource',
  UserTaskGroupController]);

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
      id: 'user-tasks-tab',
      label: 'User Tasks',
      url: 'plugin://base/static/app/views/processInstance/user-tasks-table.html',
      controller: 'UserTaskController',
      priority: 5
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
