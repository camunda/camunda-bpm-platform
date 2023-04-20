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

var angular = require('angular');
var actionTemplate = fs.readFileSync(
  __dirname + '/override-job-priority-action.html',
  'utf8'
);
var dialogTemplate = fs.readFileSync(
  __dirname + '/override-job-priority-dialog.html',
  'utf8'
);

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.jobDefinition.action', {
    id: 'job-definition-override-job-priority-action',
    template: actionTemplate,
    controller: [
      '$scope',
      '$rootScope',
      '$uibModal',
      function($scope, $rootScope, $modal) {
        $scope.openDialog = function(jobDefinition) {
          var dialog = $modal.open({
            resolve: {
              jobDefinition: function() {
                return jobDefinition;
              }
            },
            controller: 'JobDefinitionOverrideJobPriorityController',
            template: dialogTemplate
          });

          dialog.result
            .then(function(result) {
              // dialog closed. YEA!
              if (result.status === 'SUCCESS') {
                $scope.processData.changed('jobDefinitions');
                $scope.processData.set(
                  'filter',
                  angular.extend({}, $scope.filter)
                );
              }
            })
            .catch(angular.noop);
        };
      }
    ],
    priority: 10
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
