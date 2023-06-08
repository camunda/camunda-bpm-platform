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

var template = require('./setup.html?raw');

var Controller = [
  '$scope',
  'InitialUserResource',
  'Notifications',
  '$location',
  'Uri',
  '$translate',
  function(
    $scope,
    InitialUserResource,
    Notifications,
    $location,
    Uri,
    $translate
  ) {
    if (!/.*\/app\/admin\/([\w-]+)\/setup\/.*/.test($location.absUrl())) {
      $location.path('/');
      return;
    }

    $scope.engineName = Uri.appUri(':engine');

    // data model for user profile
    $scope.profile = {
      id: '',
      firstName: '',
      lastName: '',
      email: ''
    };

    $scope.created = false;

    // data model for credentials
    $scope.credentials = {
      password: '',
      password2: '',
      valid: true
    };

    $scope.createUser = function() {
      var user = {
        profile: $scope.profile,
        credentials: {password: $scope.credentials.password}
      };

      InitialUserResource.create(user)
        .$promise.then(
          function() {
            $scope.created = true;
          },
          function() {
            Notifications.addError({
              status: $translate.instant('NOTIFICATIONS_STATUS_ERROR'),
              message: $translate.instant('SETUP_COULD_NOT_CREATE_USER')
            });
          }
        )
        .catch(function() {});
    };
  }
];

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/setup', {
      template: template,
      controller: Controller
    });
  }
];
