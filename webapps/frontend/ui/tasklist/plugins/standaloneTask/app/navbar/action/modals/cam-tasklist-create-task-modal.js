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

var angular = require('angular');

module.exports = [
  '$scope',
  '$translate',
  'Notifications',
  'camAPI',
  function($scope, $translate, Notifications, camAPI) {
    var NEW_TASK = {
      name: null,
      assignee: null,
      tenantId: null,
      description: null,
      priority: 50 // default value
    };

    var Task = camAPI.resource('task');

    var task = ($scope.task = angular.copy(NEW_TASK));

    var _form = null;

    $scope.setNewTaskForm = function(innerForm) {
      _form = innerForm;
    };

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    var isValid = ($scope.isValid = function() {
      return _form && _form.$valid;
    });

    $scope.save = function() {
      if (!isValid()) {
        return;
      }
      $scope.submitInProgress = true;

      Task.create(task, function(err) {
        $scope.submitInProgress = false;
        if (err) {
          $translate('TASK_SAVE_ERROR')
            .then(function(translated) {
              Notifications.addError({
                status: translated,
                message: err ? err.message : '',
                exclusive: true,
                scope: $scope
              });
            })
            .catch(angular.noop);
        } else {
          $scope.$close();
        }
      });
    };
  }
];
