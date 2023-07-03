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

var template = require('./cam-cockpit-resource-wrapper.html?raw');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  function() {
    return {
      restrict: 'A',
      scope: {
        resourceDetailsData: '=',
        control: '=?'
      },

      template: template,

      controller: [
        '$scope',
        '$q',
        '$location',
        'Views',
        'Notifications',
        'search',
        '$translate',
        function(
          $scope,
          $q,
          $location,
          Views,
          Notifications,
          search,
          $translate
        ) {
          // utilities ///////////////////////////////////////////////////////////////////

          var errorNotification = function(src, err) {
            if (err.message) {
              var idx = err.message.indexOf('<-');
              if (idx !== -1) {
                err.message = err.message.split('<-')[1].trim();
              }
            }
            Notifications.addError({
              status: src,
              message: err ? err.message : '',
              exclusive: true,
              scope: $scope
            });
          };

          var enhanceErrorMessage = function(msg) {
            if (msg) {
              if (msg.indexOf('does not exist') === -1) {
                return $translate.instant(
                  'REPOSITORY_DEPLOYMENT_RESOURCE_DIRECTIVES_RETURN_1'
                );
              }
            }
            return $translate.instant(
              'REPOSITORY_DEPLOYMENT_RESOURCE_DIRECTIVES_RETURN_2'
            );
          };

          var clearResource = function() {
            var search = $location.search() || {};
            delete search.resource;
            delete search.resourceName;
            $location.search(angular.copy(search));
            $location.replace();
          };

          // fields /////////////////////////////////////////////////////////////////////

          var resourceData = ($scope.resourceData = $scope.resourceDetailsData.newChild(
            $scope
          ));

          var PLUGIN_DETAILS_COMPONENT = 'cockpit.repository.resource.detail';

          // observe /////////////////////////////////////////////////////////////////////

          resourceData.observe('currentDeployment', function(deployment) {
            $scope.deployment = deployment;
          });

          $scope.resourceState = resourceData.observe([
            'resource',
            'binary',
            function(resource) {
              $scope.resource = resource;
            }
          ]);

          $scope.$watch('resourceState.$error', function(err) {
            if (err) {
              var src = enhanceErrorMessage(err.message);
              errorNotification(src, err);
              clearResource();
            }
          });

          // plugins ///////////////////////////////////////////////////////////////////////

          $scope.resourceVars = {
            read: ['control', 'deployment', 'resource', 'resourceData']
          };
          $scope.resourceDetailTabs = Views.getProviders({
            component: PLUGIN_DETAILS_COMPONENT
          });
        }
      ]
    };
  }
];
