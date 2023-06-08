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

var dialogTemplate = require('./job-retry-bulk-dialog.html?raw');
var actionTemplate = require('./job-retry-bulk-action.html?raw');

module.exports = function(ngModule, pluginPoint) {
  ngModule.controller('JobRetryActionController', [
    '$scope',
    '$uibModal',
    function($scope, $modal) {
      $scope.openDialog = function() {
        $modal
          .open({
            resolve: {
              processData: function() {
                return $scope.processData;
              },
              processInstance: function() {
                return $scope.processInstance;
              }
            },
            size: 'lg',
            controller: 'JobRetriesController',
            template: dialogTemplate
          })
          .result.catch(function() {});
      };
    }
  ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView(pluginPoint, {
      id: 'job-retry-action',
      label: 'Job Retry Action',
      template: actionTemplate,
      controller: 'JobRetryActionController',
      priority: 15
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  ngModule.config(Configuration);
};
