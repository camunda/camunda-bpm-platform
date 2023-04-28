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

var angular = require('camunda-commons-ui/vendor/angular');

var GROUP_TYPE = 'candidate';

module.exports = [
  '$scope',
  '$translate',
  '$q',
  'Notifications',
  'camAPI',
  function($scope, $translate, $q, Notifications, camAPI) {
    // setup //////////////////////////////////////////////

    var Task = camAPI.resource('task');

    var task = null;

    var NEW_GROUP = {groupId: null, type: GROUP_TYPE};

    var newGroup = ($scope.newGroup = angular.copy(NEW_GROUP));

    var taskGroupsData = $scope.taskGroupsData;

    var groupsChanged = $scope.groupsChanged;

    var errorHandler = $scope.errorHandler();

    $scope._groups = [];

    var messages = {};
    $translate([
      'FAILURE',
      'INIT_GROUPS_FAILURE',
      'ADD_GROUP_FAILED',
      'REMOVE_GROUP_FAILED'
    ])
      .then(function(result) {
        messages.failure = result.FAILURE;
        messages.initGroupsFailed = result.INIT_GROUPS_FAILURE;
        messages.addGroupFailed = result.ADD_GROUP_FAILED;
        messages.removeGroupFailed = result.REMOVE_GROUP_FAILED;
      })
      .catch(angular.noop);

    // observe ////////////////////////////////////////////////////////

    $scope.modalGroupsState = taskGroupsData.observe('groups', function(
      groups
    ) {
      $scope._groups = angular.copy(groups) || [];
      $scope.validateNewGroup();
    });

    taskGroupsData.observe('task', function(_task) {
      task = _task;
    });

    // actions ///////////////////////////////////////////////////////

    $scope.$watch('modalGroupsState.$error', function(error) {
      if (error) {
        Notifications.addError({
          status: messages.failure,
          message: messages.initGroupsFailed,
          exclusive: true,
          scope: $scope
        });
      }
    });

    $scope.addGroup = function() {
      var taskId = task.id;

      groupsChanged();

      delete newGroup.error;

      $scope.loading = true;
      Task.identityLinksAdd(taskId, newGroup, function(err) {
        $scope.loading = false;
        if (err) {
          return errorHandler('TASK_UPDATE_ERROR', err);
        }

        $scope.taskGroupForm.$setPristine();

        $scope._groups.push({id: newGroup.groupId});

        newGroup = $scope.newGroup = angular.copy(NEW_GROUP);
      });
    };

    $scope.removeGroup = function(group, index) {
      var taskId = task.id;

      groupsChanged();

      Task.identityLinksDelete(
        taskId,
        {type: GROUP_TYPE, groupId: group.id},
        function(err) {
          if (err) {
            return Notifications.addError({
              status: messages.failure,
              message: messages.removeGroupFailed,
              exclusive: true,
              scope: $scope
            });
          }

          $scope._groups.splice(index, 1);
        }
      );
    };

    $scope.validateNewGroup = function() {
      delete newGroup.error;

      if ($scope.taskGroupForm && $scope.taskGroupForm.newGroup) {
        $scope.taskGroupForm.newGroup.$setValidity('duplicate', true);

        var newGroupId = newGroup.groupId;

        if (newGroupId) {
          for (
            var i = 0, currentGroup;
            (currentGroup = $scope._groups[i]);
            i++
          ) {
            if (newGroupId === currentGroup.id) {
              newGroup.error = {message: 'DUPLICATE_GROUP'};

              $scope.taskGroupForm.newGroup.$setValidity('duplicate', false);
            }
          }
        }
      }
    };

    $scope.isEnabled = function() {
      return !$scope.loading && $scope.isValid();
    };

    $scope.isValid = function() {
      if (!newGroup.groupId || newGroup.error) {
        return false;
      }

      return true;
    };
  }
];
