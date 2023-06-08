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

var addCommentTemplate = require('./cam-tasklist-task-action-comment-plugin.html?raw');
var addCommentFormTemplate = require('./modals/cam-tasklist-comment-form.html?raw');

var Controller = [
  '$scope',
  '$uibModal',
  function($scope, $modal) {
    var commentData = $scope.taskData.newChild($scope);

    commentData.observe('task', function(task) {
      $scope.task = task;
    });

    $scope.createComment = function() {
      $modal
        .open({
          // creates a child scope of a provided scope
          scope: $scope,
          //TODO: extract filter edit modal class to super style sheet
          windowClass: 'filter-edit-modal',
          size: 'lg',
          template: addCommentFormTemplate,
          controller: 'camCommentCreateModalCtrl',
          resolve: {
            task: function() {
              return $scope.task;
            }
          }
        })
        .result.then(
          function() {
            commentData.changed('task');
            document.querySelector('.createCommentLink').focus();
          },
          function() {
            document.querySelector('.createCommentLink').focus();
          }
        );
    };
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('tasklist.task.action', {
    id: 'task-action-comment',
    template: addCommentTemplate,
    controller: Controller,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
