/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');
var angular = require('angular');

var identityLinksTemplate = require('./identity-links-modal.html?raw');
var userTasksTemplate = require('./user-tasks-table.html?raw');

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
    '$scope',
    'search',
    'camAPI',
    'TaskResource',
    'Notifications',
    '$uibModal',
    '$translate',
    'localConf',
    'Uri',
    function(
      $scope,
      search,
      camAPI,
      TaskResource,
      Notifications,
      $modal,
      $translate,
      localConf,
      Uri
    ) {
      // input: processInstance, processData

      var userTaskData = $scope.processData.newChild($scope),
        processInstance = $scope.processInstance,
        taskIdIdToExceptionMessageMap,
        taskCopies;

      var DEFAULT_PAGES = {size: 50, total: 0, current: 1};

      // reset Page when changing Tabs
      $scope.$on('$destroy', function() {
        search('page', null);
      });

      var pages = ($scope.pages = angular.copy(DEFAULT_PAGES));

      var filter = null;
      var executionIdToInstanceMap = null;

      var Task = camAPI.resource('task');

      var sorting = ($scope.sorting = loadLocal({
        sortBy: 'created',
        sortOrder: 'desc'
      }));

      $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(
        null,
        'activityInstanceIdIn'
      );

      // prettier-ignore
      $scope.headColumns = [
        { class: 'activity uuid', request: 'nameCaseInsensitive', sortable: true, content: $translate.instant('PLUGIN_USER_TASKS_ACTIVITY') },
        { class: 'assignee', request: 'assignee', sortable: true, content: $translate.instant('PLUGIN_USER_TASKS_ASSIGNEE') },
        { class: 'owner', request: '', sortable: false, content: $translate.instant('PLUGIN_USER_TASKS_OWNER') },
        { class: 'created', request: 'created', sortable: true, content: $translate.instant('PLUGIN_USER_TASKS_CREATED_DATE') },
        { class: 'due', request: 'dueDate', sortable: true, content: $translate.instant('PLUGIN_USER_TASKS_DUE_DATE') },
        { class: 'follow-up', request: 'followUpDate', sortable: true, content: $translate.instant('PLUGIN_USER_TASKS_FOLLOW_UP_DATE') },
        { class: 'priority', request: 'priority', sortable: true, content: $translate.instant('PLUGIN_USER_TASKS_PRIORITY') },
        { class: 'delegation-state', request: '', sortable: false, content: $translate.instant('PLUGIN_USER_TASKS_DELEGATION_STATE') },
        { class: 'task-id uuid', request: 'id', sortable: true, content: $translate.instant('PLUGIN_USER_TASKS_TASK_ID') },
        { class: 'action', request: '', sortable: false, content: $translate.instant('PLUGIN_USER_TASKS_ACTION') }
      ];

      $scope.$watch('pages.current', function(newValue, oldValue) {
        if (newValue == oldValue) {
          return;
        }

        search('page', !newValue || newValue == 1 ? null : newValue);
      });

      userTaskData.observe(['filter', 'executionIdToInstanceMap'], function(
        newFilter,
        newExecutionIdToInstanceMap
      ) {
        pages.current = newFilter.page || 1;
        executionIdToInstanceMap = newExecutionIdToInstanceMap;

        updateView(newFilter, newExecutionIdToInstanceMap, sorting);
      });

      function loadLocal(defaultValue) {
        return localConf.get('sortPIUserTaskTab', defaultValue);
      }

      function saveLocal(sorting) {
        localConf.set('sortPIUserTaskTab', sorting);
      }

      function updateView(newFilter, executionIdToInstanceMap, sorting) {
        filter = angular.copy(newFilter);

        delete filter.page;
        delete filter.activityIds;
        delete filter.scrollToBpmnElement;

        var page = pages.current,
          count = pages.size,
          firstResult = (page - 1) * count;

        var defaultParams = {
          processInstanceId: processInstance.id,
          processDefinitionId: processInstance.definitionId,
          sorting: [
            {
              sortBy: sorting.sortBy,
              sortOrder: sorting.sortOrder
            }
          ]
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

        TaskResource.count(params)
          .$promise.then(function(response) {
            pages.total = response.count;
          })
          .catch(angular.noop);

        TaskResource.query(pagingParams, params)
          .$promise.then(function(response) {
            for (var i = 0, task; (task = response[i]); i++) {
              task.instance = executionIdToInstanceMap[task.executionId];
              taskCopies[task.id] = angular.copy(task);
            }

            $scope.userTasks = response;
            $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
          })
          .catch(angular.noop);
      }

      $scope.onSortChange = function(_sorting) {
        sorting = _sorting;
        saveLocal(sorting);
        updateView(filter, executionIdToInstanceMap, sorting);
      };

      $scope.getHref = function(userTask) {
        if (userTask.instance) {
          return (
            '#/process-instance/' +
            processInstance.id +
            '/runtime?tab=user-tasks-tab&' +
            $scope.getSearchQueryForSearchType(userTask.instance.id)
          );
        }

        return '';
      };

      $scope.getTasklistHref = function(userTask) {
        return Uri.appUri('tasklistbase://:engine/#/?task=' + userTask.id);
      };

      $scope.submitAssigneeChange = function(editForm, cb) {
        cb = ensureCallback(cb);

        var userTask = editForm.context;
        var copy = taskCopies[userTask.id];
        var defaultParams = {id: userTask.id};
        var params = {userId: editForm.value || null};

        TaskResource.setAssignee(defaultParams, params).$promise.then(
          // success
          function(response) {
            var assignee = (copy.assignee = userTask.assignee =
              response.userId);

            var message;
            if (assignee) {
              message = $translate.instant('PLUGIN_USER_TASKS_MESSAGE_1', {
                name: userTask.instance.name,
                assignee: copy.assignee
              });
            } else {
              message = $translate.instant('PLUGIN_USER_TASKS_MESSAGE_2', {
                name: userTask.instance.name
              });
            }

            Notifications.addMessage({
              status: $translate.instant('PLUGIN_USER_TASKS_STATUS_ASSIGNEE'),
              message: message,
              duration: 5000
            });

            cb();
          },

          // error
          function(error) {
            var message;
            if (userTask.assignee) {
              message = $translate.instant('PLUGIN_USER_TASKS_MESSAGE_3', {
                name: userTask.instance.name,
                assignee: userTask.assignee,
                error: error.data.message
              });
            } else {
              message = $translate.instant('PLUGIN_USER_TASKS_MESSAGE_4', {
                name: userTask.instance.name,
                error: error.data.message
              });
            }

            var err = {
              status: $translate.instant('PLUGIN_USER_TASKS_STATUS_ASSIGNEE'),
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
          var identityLinks = compact(
            map(response, function(item) {
              var ok =
                item[decorator.key] &&
                item.type !== 'assignee' &&
                item.type !== 'owner';
              return ok ? item : null;
            })
          );

          // 3. open a dialog
          $modal
            .open({
              resolve: {
                userTask: function() {
                  return userTask;
                },
                identityLinks: function() {
                  return identityLinks;
                },
                decorator: function() {
                  return decorator;
                }
              },
              controller: 'IdentityLinksController',
              template: identityLinksTemplate,
              windowClass: 'identity-link-modal'
            })
            .result.catch(angular.noop);
        });
      };

      $scope.changeGroupIdentityLinks = function() {
        var userTask = this.userTask;

        $scope.openDialog(userTask, {
          title: $translate.instant('PLUGIN_USER_TASKS_MANAGE_GROUPS'),
          table: {
            label: $translate.instant('PLUGIN_USER_TASKS_CURRENT_GROUPS'),
            id: $translate.instant('PLUGIN_USER_TASKS_GROUP_ID')
          },
          add: {
            label: $translate.instant('PLUGIN_USER_TASKS_ADD_GROUP')
          },
          notifications: {
            remove: $translate.instant(
              'PLUGIN_USER_TASKS_NOTIFICATION_REMOVE_GROUP'
            ),
            add: $translate.instant('PLUGIN_USER_TASKS_NOTIFICATION_ADD_GROUP')
          },
          key: 'groupId'
        });
      };

      $scope.changeUserIdentityLinks = function() {
        var userTask = this.userTask;

        $scope.openDialog(userTask, {
          title: $translate.instant('PLUGIN_USER_TASKS_MANAGE_USERS'),
          table: {
            label: $translate.instant('PLUGIN_USER_TASKS_CURRENT_USERS'),
            id: $translate.instant('PLUGIN_USER_TASKS_USER_ID')
          },
          add: {
            label: $translate.instant('PLUGIN_USER_TASKS_ADD_USER')
          },
          notifications: {
            remove: $translate.instant(
              'PLUGIN_USER_TASKS_NOTIFICATION_REMOVE_USER'
            ),
            add: $translate.instant('PLUGIN_USER_TASKS_NOTIFICATION_ADD_USER')
          },
          key: 'userId'
        });
      };

      $scope.getExceptionForUserTask = function(userTask) {
        return taskIdIdToExceptionMessageMap[userTask.id];
      };

      // translate
      $scope.translate = function(token, object) {
        return $translate.instant(token, object);
      };
    }
  ]);

  ngModule.controller('IdentityLinksController', [
    '$uibModalInstance',
    'camAPI',
    '$scope',
    'Notifications',
    'userTask',
    'identityLinks',
    'decorator',
    function(
      $modalInstance,
      camAPI,
      $scope,
      Notifications,
      userTask,
      identityLinks,
      decorator
    ) {
      var Task = camAPI.resource('task');

      $scope.identityLinks = identityLinks;
      $scope.decorator = decorator;

      $scope.title = decorator.title;
      var key = ($scope.key = decorator.key);

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
          identityLinks = $scope.identityLinks = compact(
            map(identityLinks, function(g, d) {
              return delta !== d ? g : false;
            })
          );
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
          exists = exists || identityLink[key] === newItem;
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
    }
  ]);

  var Configuration = function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
      id: 'user-tasks-tab',
      label: 'PLUGIN_USER_TASKS_LABEL',
      template: userTasksTemplate,
      controller: 'UserTaskController',
      priority: 5
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  ngModule.config(Configuration);
};
