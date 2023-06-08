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

var actionTemplate = require('./update-suspension-state-action.html?raw');
var dialogTemplate = require('./update-suspension-state-dialog.html?raw');
var angular = require('angular');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
    id: 'update-suspension-state-action',
    label: 'PLUGIN_UPDATE_SUSPENSION_STATE',
    template: actionTemplate,
    controller: [
      '$scope',
      '$rootScope',
      '$uibModal',
      function($scope, $rootScope, $modal) {
        $scope.openDialog = function() {
          var dialog = $modal.open({
            resolve: {
              processData: function() {
                return $scope.processData;
              },
              processInstance: function() {
                return $scope.processInstance;
              }
            },
            controller: 'UpdateProcessInstanceSuspensionStateController',
            template: dialogTemplate
          });

          dialog.result
            .then(function(result) {
              // dialog closed. YEA!
              if (result.status === 'SUCCESS') {
                $scope.processInstance.suspended = result.suspended;
                $rootScope.$broadcast(
                  '$processInstance.suspensionState.changed',
                  $scope.processInstance
                );

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
    priority: 5
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
