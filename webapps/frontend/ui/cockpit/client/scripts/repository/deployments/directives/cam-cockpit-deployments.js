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
var lodash = require('../../../../../../../camunda-commons-ui/vendor/lodash');

var template = require('./cam-cockpit-deployments.html')();
var searchConfigJSON = require('./cam-cockpit-deployments-search-plugin-config.json');

var debouncePromiseFactory = require('camunda-bpm-sdk-js').utils
  .debouncePromiseFactory;
var debounceQuery = debouncePromiseFactory();

module.exports = [
  function() {
    return {
      restrict: 'A',
      scope: {
        deploymentsData: '=',
        totalItems: '=',
        deployments: '='
      },
      template: template,
      controller: [
        '$scope',
        '$location',
        '$rootScope',
        'search',
        'Notifications',
        'camAPI',
        function($scope, $location, $rootScope, search, Notifications, camAPI) {
          var Deployment = camAPI.resource('deployment');
          var deploymentsListData = ($scope.deploymentsListData = $scope.deploymentsData.newChild(
            $scope
          ));
          $scope.searchConfig = searchConfigJSON;

          $scope.loadingState = 'INITIAL';

          // control ///////////////////////////////////////////////////////////////////
          var control = ($scope.control = {});
          control.addMessage = function(status, msg, unsafe) {
            Notifications.addMessage({
              status: status,
              message: msg,
              scope: $scope,
              unsafe: unsafe
            });
          };

          $scope.onSearchChange = function(query, pages) {
            $scope.loadingState = 'LOADING';
            var pagination = {
              firstResult: (pages.current - 1) * pages.size,
              maxResults: pages.size
            };

            return debounceQuery(
              Deployment.list(
                lodash.assign(query, pagination, $scope.deploymentsSorting)
              )
            )
              .then(function(res) {
                $scope.deployments = res.items;

                var phase = $scope.$root.$$phase;
                if (phase !== '$apply' && phase !== '$digest') {
                  $scope.$apply(function() {
                    $scope.loadingState = 'LOADED';
                  });
                }

                return res.count;
              })
              .catch(function() {
                var phase = $scope.$root.$$phase;
                if (phase !== '$apply' && phase !== '$digest') {
                  $scope.$apply(function() {
                    $scope.loadingState = 'ERROR';
                  });
                }
              });
          };

          // observe data ///////////////////////////////////////////////////////////////
          deploymentsListData.observe('currentDeployment', function(
            currentDeployment
          ) {
            $scope.currentDeployment = currentDeployment;
          });

          deploymentsListData.observe('deploymentsSorting', function(
            deploymentsSorting
          ) {
            $scope.deploymentsSorting = deploymentsSorting;

            $rootScope.$broadcast(
              'cam-common:cam-searchable:query-force-change'
            );
          });

          // selection ////////////////////////////////////////////////////////////////
          $scope.focus = function(deployment) {
            if (!isFocused(deployment)) {
              search.updateSilently({
                resource: null,
                resourceName: null,
                viewbox: null,
                editMode: true
              });
            }

            search.updateSilently({
              deployment: deployment.id
            });
            deploymentsListData.changed('currentDeployment');
          };

          var isFocused = ($scope.isFocused = function(deployment) {
            return (
              deployment &&
              $scope.currentDeployment &&
              deployment.id === $scope.currentDeployment.id
            );
          });
        }
      ]
    };
  }
];
