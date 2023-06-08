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

var template = require('./cam-cockpit-resource-action-download-plugin.html?raw');

var Controller = [
  '$scope',
  function($scope) {
    // fields ////////////////////////////////////////////

    var downloadData = $scope.resourceDetailsData.newChild($scope);

    // observe //////////////////////////////////////////

    downloadData.observe('resource', function(_resource) {
      $scope.resource = _resource;
    });

    downloadData.observe('currentDeployment', function(_deployment) {
      $scope.deployment = _deployment;
    });

    // download link /////////////////////////////////////

    $scope.downloadLink = $scope.control.downloadLink;
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.repository.resource.action', {
    id: 'download-resource',
    controller: Controller,
    template: template,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
