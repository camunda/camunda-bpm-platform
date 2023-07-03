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

var template = require('./cam-cockpit-delete-deployment-plugin.html?raw');
var modalTemplate = require('./modals/cam-cockpit-delete-deployment-modal.html?raw');

var Controller = [
  '$scope',
  '$uibModal',
  '$rootScope',
  function($scope, $modal, $rootScope) {
    var deploymentData = $scope.deploymentData;

    $scope.deleteDeployment = function($event, deployment) {
      $event.stopPropagation();

      $modal
        .open({
          controller: 'camDeleteDeploymentModalCtrl',
          template: modalTemplate,
          resolve: {
            deploymentData: function() {
              return deploymentData;
            },
            deployment: function() {
              return deployment;
            }
          }
        })
        .result.then(function() {
          $rootScope.$broadcast('cam-common:cam-searchable:query-force-change');
        })
        .catch(function() {});
    };
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.repository.deployment.action', {
    id: 'delete-deployment',
    template: template,
    controller: Controller,
    priority: 1000
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
