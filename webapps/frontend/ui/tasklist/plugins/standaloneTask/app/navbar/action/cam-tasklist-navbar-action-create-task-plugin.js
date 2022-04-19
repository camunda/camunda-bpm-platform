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

var fs = require('fs');

var createTaskActionTemplate = require('./cam-tasklist-navbar-action-create-task-plugin.html')();
var createTaskModalTemplate = require('./modals/cam-tasklist-create-task-modal.html')();

var Controller = [
  '$scope',
  '$uibModal',
  '$timeout',
  function($scope, $modal, $timeout) {
    $scope.open = function() {
      var modalInstance = $modal.open({
        size: 'lg',
        controller: 'camCreateTaskModalCtrl',
        template: createTaskModalTemplate
      });

      modalInstance.result.then(
        function() {
          $scope.$root.$broadcast('refresh');
          document.querySelector('.create-task-action a').focus();
        },
        function() {
          document.querySelector('.create-task-action a').focus();
        }
      );

      // once we upgrade to a newer version of angular and angular-ui-bootstrap,
      // we can use the {{rendered}} promise to get rid of the $timeouts
      modalInstance.opened
        .then(function() {
          $timeout(function() {
            $timeout(function() {
              document.querySelectorAll('div.modal-content input')[0].focus();
            });
          });
        })
        .catch(function() {});
    };
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('tasklist.navbar.action', {
    id: 'create-task-action',
    template: createTaskActionTemplate,
    controller: Controller,
    priority: 200
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
