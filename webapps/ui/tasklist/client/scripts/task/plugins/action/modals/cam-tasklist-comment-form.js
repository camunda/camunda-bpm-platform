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

module.exports = [
  '$scope',
  '$translate',
  'Notifications',
  'camAPI',
  'task',
  'configuration',
  function($scope, $translate, Notifications, camAPI, task, configuration) {
    var Task = camAPI.resource('task');

    $scope.comment = {message: ''};

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    function errorNotification(src, err) {
      $translate(src)
        .then(function(translated) {
          Notifications.addError({
            status: translated,
            message: err ? err.message : '',
            exclusive: true,
            scope: $scope
          });
        })
        .catch(function() {});
    }

    $scope.submit = function() {
      let data = {message: $scope.comment.message};
      if (
        configuration.getAssignProcessInstanceIdToTaskComment() &&
        task.processInstanceId
      ) {
        data = {...data, processInstanceId: task.processInstanceId};
      }

      Task.createComment(task.id, data, function(err) {
        if (err) {
          return errorNotification('COMMENT_SAVE_ERROR', err);
        }

        $scope.$close();
      });
    };
  }
];
