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
/* jshint browserify: true */
var fs = require('fs');
var template = fs.readFileSync(__dirname + '/user-profile.html', 'utf8');
var angular = require('../../../../../camunda-commons-ui/vendor/angular');

module.exports = [
  'camAPI',
  'Notifications',
  '$translate',
  function(camAPI, Notifications, $translate) {
    return {
      restrict: 'A',

      template: template,

      scope: {
        username: '='
      },

      replace: true,

      link: function($scope) {
        $scope.visibleForm = null;
        $scope.showForm = function(name) {
          $scope.visibleForm = name || null;
        };

        $scope.processing = false;
        $scope.user = {
          id: $scope.username
        };

        $scope.password = {
          current: null,
          new: null,
          confirmation: null,
          valid: true
        };

        var groupPages = ($scope.groupPages = {
          current: 1,
          size: 25,
          total: 0
        });

        var groupResource = camAPI.resource('group');

        groupResource.count().then(function(res) {
          groupPages.total = res.count;
        });

        $scope.loadGroups = function() {
          groupResource.list(
            {
              firstResult: groupPages.size * (groupPages.current - 1),
              maxResults: groupPages.size,
              member: $scope.user.id
            },
            function(err, groups) {
              if (err) {
                throw err;
              }
              $scope.user.groups = groups;
            }
          );
        };

        $scope.loadGroups();

        var userResource = camAPI.resource('user');
        userResource.profile(
          {
            id: $scope.user.id
          },
          function(err, data) {
            $scope.persistedProfile = data;
            angular.extend($scope.user, data);
            $scope.$root.userFullName = data.firstName + ' ' + data.lastName;
          }
        );

        $scope.submitProfile = function() {
          $scope.processing = true;
          userResource.updateProfile($scope.user, function(err) {
            $scope.processing = false;

            if (!err) {
              $scope.userProfile.$setPristine();

              $scope.persistedProfile = angular.copy($scope.user);

              Notifications.addMessage({
                status: $translate.instant('CHANGES_SAVED'),
                message: '',
                http: true,
                exclusive: ['http'],
                duration: 5000
              });

              $scope.showForm();
            } else {
              Notifications.addMessage({
                status: $translate.instant('ERROR_WHILE_SAVING'),
                message: err.message,
                http: true,
                exclusive: ['http'],
                duration: 5000
              });
            }
          });
        };

        function checkPassword() {
          $scope.passwordsMismatch =
            $scope.changePassword.confirmation.$dirty &&
            $scope.password.new !== $scope.password.confirmation;

          $scope.changePassword.confirmation.$setValidity(
            'mismatch',
            !$scope.passwordsMismatch
          );
        }

        $scope.$watch('password.new', checkPassword);
        $scope.$watch('password.confirmation', checkPassword);
        $scope.$watch('changePassword.new.$dirty', checkPassword);
        $scope.$watch('changePassword.confirmation.$dirty', checkPassword);

        $scope.submitPassword = function() {
          $scope.processing = true;

          userResource.updateCredentials(
            {
              id: $scope.user.id,
              password: $scope.password.confirmation,
              authenticatedUserPassword: $scope.password.current
            },
            function(err) {
              $scope.processing = false;

              if (!err) {
                $scope.changePassword.$setPristine();
                $scope.password = {
                  current: null,
                  new: null,
                  confirmation: null
                };

                Notifications.addMessage({
                  status: $translate.instant('PASSWORD_CHANGED'),
                  message: '',
                  http: true,
                  exclusive: ['http'],
                  duration: 5000
                });

                $scope.showForm();
              } else {
                Notifications.addMessage({
                  status: $translate.instant('ERROR_WHILE_CHANGING_PASSWORD'),
                  message: err.message,
                  http: true,
                  exclusive: ['http'],
                  duration: 5000
                });
              }
            }
          );
        };

        // translate
        $scope.translate = function(token, object) {
          return $translate.instant(token, object);
        };
      }
    };
  }
];
