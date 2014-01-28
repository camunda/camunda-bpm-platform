ngDefine('cockpit.plugin.base.views', function(module) {

   function UserTaskController ($scope, search, TaskResource, Notifications) {

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
      return '#/process-instance/' + processInstance.id + '?activityInstanceIds=' + userTask.instance.id;
    };

    $scope.editAssignee = function (userTask) {
      userTask.inEditMode = true;
    };

    $scope.closeInPlaceEditing = function (userTask) {
      delete userTask.inEditMode;

      // clear the exception for the passed user task
      taskIdIdToExceptionMessageMap[userTask.id] = null;

      // reset the values of the copy
      var copy = taskCopies[userTask.id];
      angular.extend(copy, userTask);

    };

    $scope.getCopy = function (userTaskId) {
      return taskCopies[userTaskId];
    };

    var isValid = $scope.isValid = function (editAssigneeFrom) {
      if (editAssigneeFrom.$invalid) {
        return false;
      }

      return true;
    }

    $scope.submit = function (editAssigneeFrom, userTask) {
      if (!isValid(editAssigneeFrom)) {
        return;
      }

      var copy = taskCopies[userTask.id],
          defaultParams = {id: userTask.id},
          params = {userId : copy.assignee};

      // If the value did not change then there is nothing to do!
      if (userTask.assignee === copy.assignee) {
        $scope.closeInPlaceEditing(userTask);
        return;
      }

      TaskResource.setAssignee(defaultParams, params).$then(

        // success
        function (response) {
          Notifications.addMessage({ status: 'Assignee', message: 'The assignee of the user task \'' + userTask.instance.name + '\' has been set to \'' + copy.assignee + '\' successfully.', duration: 5000 });
          angular.extend(userTask, copy);
          $scope.closeInPlaceEditing(userTask);
        },

        // error
        function (error) {
          Notifications.addError({ status: 'Assignee', message: 'The assignee of the user task \'' + userTask.instance.name + '\' could not be set to \'' + copy.assignee + '\' successfully.', exclusive: true, duration: 5000 });
          taskIdIdToExceptionMessageMap[userTask.id] = error.data;
        }
      );
    };

    $scope.getExceptionForUserTask = function (userTask) {
      return taskIdIdToExceptionMessageMap[userTask.id];
    };

    $scope.selectActivity = function(activityId, event) {
      event.preventDefault();
      $scope.processData.set('filter', angular.extend({}, $scope.filter, {
        activityInstanceIds: [activityId],
        activityIds: [activityId.split(':').shift()]
      }));
    };
  };

  module.controller('UserTaskController', [ '$scope', 'search', 'TaskResource', 'Notifications', UserTaskController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.live.tab', {
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
