'use strict';

var fs = require('fs');
var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');
var angular = require('angular');

var identityLinksTemplate = fs.readFileSync(__dirname + '/identity-links-modal.html', 'utf8');
var userTasksTemplate = fs.readFileSync(__dirname + '/user-tasks-table.html', 'utf8');

module.exports = function(ngModule) {

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
      if (val) {
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
    '$scope', 'search', 'camAPI', 'TaskResource', 'Notifications', '$modal',
    function($scope,   search,   camAPI,   TaskResource,   Notifications,   $modal) {

      // input: processInstance, processData

      var userTaskData = $scope.processData.newChild($scope),
          processInstance = $scope.processInstance,
          taskIdIdToExceptionMessageMap,
          taskCopies;

      var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };

      var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

      var filter = null;

      var Task = camAPI.resource('task');

      $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(null, 'activityInstanceIdIn');

      $scope.$watch('pages.current', function(newValue, oldValue) {
        if (newValue == oldValue) {
          return;
        }

        search('page', !newValue || newValue == 1 ? null : newValue);
      });

      userTaskData.observe([ 'filter', 'executionIdToInstanceMap' ], function(newFilter, executionIdToInstanceMap) {
        pages.current = newFilter.page || 1;

        updateView(newFilter, executionIdToInstanceMap);
      });

      function updateView(newFilter, executionIdToInstanceMap) {
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
        $scope.loadingState = 'LOADING';

        taskIdIdToExceptionMessageMap = {};
        taskCopies = {};

        TaskResource.count(params).$promise.then(function(response) {
          pages.total = response.count;
        });

        TaskResource.query(pagingParams, params).$promise.then(function(response) {
          for (var i = 0, task; (task = response[i]); i++) {
            task.instance = executionIdToInstanceMap[task.executionId];
            taskCopies[task.id] = angular.copy(task);
          }

          $scope.userTasks = response;
          $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
        });

      }

      $scope.getHref = function(userTask) {
        if(userTask.instance) {
          return '#/process-instance/' + processInstance.id + '/runtime?tab=user-tasks-tab&' +
            $scope.getSearchQueryForSearchType(userTask.instance.id);
        }

        return '';
      };

      $scope.submitAssigneeChange = function(editForm, cb) {
        cb = ensureCallback(cb);

        var userTask = editForm.context;
        var copy = taskCopies[userTask.id];
        var defaultParams = {id: userTask.id};
        var params = {userId : editForm.value};

        TaskResource.setAssignee(defaultParams, params).$promise.then(
          // success
          function(response) {
            var assignee = copy.assignee = userTask.assignee = response.userId;

            var message;
            if (assignee) {
              message = 'The assignee of the user task \'' +
                         userTask.instance.name +
                         '\' has been set to \'' +
                         copy.assignee + '\' successfully.';
            }
            else {
              message = 'The assignee of the user task \'' +
                         userTask.instance.name +
                         '\' has been reset successfully.';
            }

            Notifications.addMessage({
              status: 'Assignee',
              message: message,
              duration: 5000
            });

            cb();
          },

          // error
          function(error) {
            var message;
            if (userTask.assignee) {
              message = 'The assignee of the user task \'' +
                         userTask.instance.name +
                         '\' could not be set to \'' + userTask.assignee +
                         '\'. ' + error.data.message;
            }
            else {
              message = 'The assignee of the user task \'' +
                         userTask.instance.name +
                         '\' could not be reset. ' + error.data.message;
            }

            var err = {
              status: 'Assignee',
              message: message,
              exclusive: true
            };

            userTask.assignee = copy.assignee;

            Notifications.addError(err);
            taskIdIdToExceptionMessageMap[userTask.id] = error.data;
            cb(err);
          }
        );
      };

      $scope.openDialog = function(userTask, decorator) {

        // 1. load the identityLinks
        Task.identityLinks(userTask.id, function(err, response) {

          // 2. filter the response.data to exclude links
          var identityLinks = compact(map(response, function(item) {
            var ok = item[decorator.key] && item.type !== 'assignee' && item.type !== 'owner';
            return ok ? item : null;
          }));

          // 3. open a dialog
          $modal.open({
            resolve: {
              userTask: function() { return userTask; },
              identityLinks: function() { return identityLinks; },
              decorator: function() { return decorator; }
            },
            controller: 'IdentityLinksController',
            template: identityLinksTemplate,
            windowClass:  'identity-link-modal'
          });
        });

      };

      $scope.changeGroupIdentityLinks = function() {
        var userTask = this.userTask;

        $scope.openDialog(userTask, {
          title: 'Manage groups',
          table: {
            label: 'Current group(s)',
            id: 'Group ID'
          },
          add: {
            label: 'Add a group'
          },
          notifications: {
            remove: 'Could not remove group',
            add: 'Could not add group'
          },
          key: 'groupId'
        });
      };

      $scope.changeUserIdentityLinks = function() {
        var userTask = this.userTask;

        $scope.openDialog(userTask, {
          title: 'Manage users',
          table: {
            label: 'Current user(s)',
            id: 'User ID'
          },
          add: {
            label: 'Add a user'
          },
          notifications: {
            remove: 'Could not remove user',
            add: 'Could not add user'
          },
          key: 'userId'
        });
      };

      $scope.getExceptionForUserTask = function(userTask) {
        return taskIdIdToExceptionMessageMap[userTask.id];
      };

    }]);

  ngModule.controller('IdentityLinksController', [
    '$modalInstance', 'camAPI', '$scope', 'Notifications', 'userTask', 'identityLinks', 'decorator',
    function($modalInstance,   camAPI,   $scope,   Notifications,   userTask,   identityLinks,   decorator) {

      var Task = camAPI.resource('task');

      $scope.identityLinks = identityLinks;
      $scope.decorator = decorator;

      $scope.title = decorator.title;
      var key = $scope.key = decorator.key;

      $scope.$on('$routeChangeStart', function() {
        $modalInstance.close();
      });

      $scope.removeItem = function() {
        var delta = this.delta;

        Task.identityLinksDelete(userTask.id, this.identityLink, function(err) {

          if (err) {
            return Notifications.addError({
              status: decorator.notifications.remove,
              message: err.message,
              exclusive: true
            });
          }

          // deleting an entry is not enough, we need to "rebuild" the identiy links
          identityLinks = $scope.identityLinks = compact(map(identityLinks, function(g, d) {
            return delta !== d ? g : false;
          }));

        });
      };

      $scope.invalid = function() {
        var editForm = this.editForm;

        if (editForm.$invalid) {
          return true;
        }

        var exists;
        var newItem = editForm.newItem.$modelValue;
        angular.forEach(identityLinks, function(identityLink) {
          exists = (exists || (identityLink[key] === newItem));
        });

        return exists;
      };

      $scope.addItem = function() {
        var editForm = this;

        var newIdentityLink = {
          type: 'candidate'
        };

        newIdentityLink[key] = editForm.newItem;

        Task.identityLinksAdd(userTask.id, newIdentityLink, function(err) {

          if (err) {
            return Notifications.addError({
              status: decorator.notifications.add,
              message: err.message,
              exclusive: true
            });
          }

          identityLinks.push(newIdentityLink);
          editForm.newItem = '';

        });
      };

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
