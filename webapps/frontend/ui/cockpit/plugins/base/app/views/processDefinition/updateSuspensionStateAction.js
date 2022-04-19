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

var actionTemplate = require('./update-suspension-state-action.html')();
var dialogTemplate = require('./update-suspension-state-dialog.html')();
var angular = require('angular');

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView(
      'cockpit.processDefinition.runtime.action',
      {
        id: 'update-suspension-state-action',
        label: 'PLUGIN_UPDATE_SUSPENSION_STATE',
        template: actionTemplate,
        controller: [
          '$scope',
          '$uibModal',
          function($scope, $modal) {
            $scope.openDialog = function() {
              var dialog = $modal.open({
                resolve: {
                  processData: function() {
                    return $scope.processData;
                  },
                  processDefinition: function() {
                    return $scope.processDefinition;
                  }
                },
                controller: 'UpdateProcessDefinitionSuspensionStateController',
                template: dialogTemplate
              });

              dialog.result
                .then(function(result) {
                  // dialog closed. YEA!
                  if (result.status === 'SUCCESS') {
                    if (result.executeImmediately) {
                      $scope.processDefinition.suspended = result.suspended;
                    }

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
        priority: 50
      }
    );
  }
];
