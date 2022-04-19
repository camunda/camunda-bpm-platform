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

var template = require('./userCreate.html')();

var Controller = [
  '$scope',
  'page',
  'UserResource',
  'Notifications',
  '$location',
  '$translate',
  function($scope, page, UserResource, Notifications, $location, $translate) {
    $scope.$root.showBreadcrumbs = true;

    page.titleSet($translate.instant('USERS_CREATE_USER'));

    page.breadcrumbsClear();

    page.breadcrumbsAdd([
      {
        label: $translate.instant('USERS_USERS'),
        href: '#/users/'
      },
      {
        label: $translate.instant('USERS_CREATE'),
        href: '#/users-create'
      }
    ]);

    // data model for user profile
    $scope.profile = {
      id: '',
      firstName: '',
      lastName: '',
      email: ''
    };

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

      UserResource.createUser(user).$promise.then(
        function() {
          Notifications.addMessage({
            type: 'success',
            status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
            message: $translate.instant('USERS_CREATE_SUCCESS', {
              user: user.profile.id
            })
          });
          $location.path('/users');
        },
        function(err) {
          Notifications.addError({
            status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
            message: $translate.instant('USERS_CREATE_FAILED', {
              message: err.data.message
            }),
            exclusive: true
          });
        }
      );
    };
  }
];

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/user-create', {
      template: template,
      controller: Controller,
      authentication: 'required'
    });
  }
];
