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
var actionTemplate = require('./bulk-override-job-priority-action.html?raw');
var dialogTemplate = require('./bulk-override-job-priority-dialog.html?raw');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView(
    'cockpit.processDefinition.runtime.action',
    {
      id: 'bulk-job-definition-override-job-priority-action',
      template: actionTemplate,
      controller: [
        '$scope',
        '$rootScope',
        '$uibModal',
        function($scope, $rootScope, $modal) {
          var processData = $scope.processData.newChild($scope);

          var jobDefinitions;

          $scope.openDialog = function() {
            var dialog = $modal.open({
              resolve: {
                jobDefinitions: function() {
                  return jobDefinitions;
                },
                processData: function() {
                  return processData;
                }
              },
              controller: 'BulkJobDefinitionOverrideJobPriorityController',
              template: dialogTemplate
            });

            dialog.result
              .then(function(result) {
                // dialog closed. YEA!
                if (result.status === 'FINISHED') {
                  processData.changed('jobDefinitions');
                  processData.set('filter', angular.extend({}, $scope.filter));
                }
              })
              .catch(angular.noop);
          };
        }
      ],
      priority: 10
    }
  );
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
