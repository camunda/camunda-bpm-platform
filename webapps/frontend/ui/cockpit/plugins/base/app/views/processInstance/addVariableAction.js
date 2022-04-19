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
var fs = require('fs');

var actionTemplate = require('./add-variable-action.html')();
var addTemplate = require('../../../../../client/scripts/components/variables/variable-add-dialog');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
    id: 'add-variable-action',
    label: 'Add Variable Action',
    template: actionTemplate,
    controller: [
      '$scope',
      '$uibModal',
      '$rootScope',
      function($scope, $modal, $rootScope) {
        $scope.openDialog = function() {
          var dialog = $modal.open({
            scope: $scope,
            resolve: {
              resolved: () => ({
                type: 'PROCESS_INSTANCE',
                instanceId: $scope.processInstance.id
              })
            },
            controller: addTemplate.controller,
            template: addTemplate.template
          });

          dialog.result
            .then(function(result) {
              // dialog closed. YEA!
              if (result === 'SUCCESS') {
                // refresh filter and all views
                $scope.processData.set(
                  'filter',
                  angular.extend({}, $scope.filter)
                );
                $rootScope.$broadcast(
                  'cam-common:cam-searchable:query-force-change'
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
