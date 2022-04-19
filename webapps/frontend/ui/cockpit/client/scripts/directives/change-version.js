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

var template = require('./change-version.html')();

module.exports = [
  '$timeout',
  '$location',
  'ProcessDefinitionResource',
  'camAPI',
  'Notifications',
  '$translate',
  function(
    $timeout,
    $location,
    ProcessDefinitionResource,
    camAPI,
    Notifications,
    $translate
  ) {
    return {
      restrict: 'A',
      template: template,
      scope: {
        definition: '=changeVersion',
        type: '@',
        history: '@?',
        noRedirect: '@?'
      },
      link: function($scope, element) {
        $scope.model = {};
        $scope.model.newVersion =
          typeof $scope.definition.version === 'number'
            ? $scope.definition.version
            : 1;

        $scope.isValid = true;
        $scope.isValidating = true;

        $scope.isActive = false;

        $scope.enableValidating = function() {
          $scope.isValidating = true;
        };

        $scope.changeLocation = function() {
          $scope.isActive = false;
          $scope.definition.version = parseInt($scope.model.newVersion, 10);
          element
            .parent()
            .find('.dropdown-toggle')
            .show();

          if ($scope.noRedirect === undefined) {
            $timeout(function() {
              var path = '/';
              if ($scope.type === 'drd') {
                path += 'decision-requirement/';
              } else {
                path += $scope.type + '-definition/';
              }

              path += $scope.newDefinition;
              if ($scope.history !== undefined) {
                path += '/history';
              }

              $location.path(path);
            });
          }
        };

        $scope.change = function(form) {
          $scope.isValid = form.$valid;

          if (
            $scope.model.newVersion < 1 ||
            !/^[0-9]{1,7}$/.test($scope.model.newVersion)
          ) {
            $scope.isValid = false;
            return;
          }

          if ($scope.isValid) {
            var definition = $scope.definition;

            var queryParams = {
              key: definition.key,
              version: $scope.model.newVersion,
              maxResults: 2
            };

            if (definition.tenantId) {
              queryParams.tenantIdIn = [definition.tenantId];
            } else {
              queryParams.withoutTenantId = true;
            }

            var definitions;
            if ($scope.type === 'process') {
              definitions = ProcessDefinitionResource.query(queryParams)
                .$promise;
            } else if ($scope.type === 'drd') {
              definitions = camAPI.resource('drd').list(queryParams);
            } else {
              definitions = camAPI
                .resource($scope.type + '-definition')
                .list(queryParams);
            }

            definitions.then(data => {
              if (data.length > 1) {
                $scope.isValid = false;
                $scope.isValidating = false;

                Notifications.addError({
                  status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                  message: $translate.instant(
                    'DEF_VIEW_CHANGE_VERSION_NOT_UNIQUE'
                  )
                });
              } else {
                $scope.isValid = data.length === 1;
                $scope.isValidating = false;

                if ($scope.isValid) {
                  $scope.newDefinition = data[0].id;
                }
              }
            });
          }
        };

        $scope.open = function() {
          $scope.model.newVersion =
            typeof $scope.definition.version === 'number'
              ? $scope.definition.version
              : 1;

          $scope.storedVersion = $scope.model.newVersion;
          $scope.isActive = true;
          element
            .parent()
            .find('.dropdown-toggle')
            .hide();
        };

        $scope.close = function(form) {
          // cancel debounce
          form.$rollbackViewValue();

          $scope.model.newVersion = $scope.storedVersion;
          $scope.isActive = false;
          element
            .parent()
            .find('.dropdown-toggle')
            .show();
        };

        $scope.$watch(
          'isActive',
          function() {
            $timeout(function() {
              if ($scope.isActive) {
                element
                  .parent()
                  .find('input')
                  .focus();
              }
            });
          },
          true
        );
      }
    };
  }
];
