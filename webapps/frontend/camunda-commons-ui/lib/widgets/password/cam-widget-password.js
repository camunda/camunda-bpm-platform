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

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  template = require('./cam-widget-password.html?raw');

module.exports = [
  'camAPI',
  'debounce',
  '$timeout',
  function(camAPI, debounce, $timeout) {
    return {
      scope: {
        profile: '=camWidgetPasswordProfile',
        password: '=camWidgetPasswordPassword',
        isValid: '=?camWidgetPasswordValid'
      },

      link: function($scope) {
        var passwordPolicyProvider = camAPI.resource('password-policy');
        var variablePolicyIsActive = false;
        $scope.loadingState = 'DEACTIVATED';

        function createRules() {
          passwordPolicyProvider.get().then(function(res) {
            if (!res) {
              variablePolicyIsActive = false;
              $scope.isValid = true;
              $scope.loadingState = 'DEACTIVATED';
              return;
            }
            variablePolicyIsActive = true;
            $scope.tooltip = 'PASSWORD_POLICY_TOOLTIP';
            $scope.rules = res.rules;

            // update State
            handlePasswordUpdate();
          });
        }
        createRules();

        var handlePasswordUpdate = function() {
          if (!variablePolicyIsActive) return;

          $scope.isValid = false;
          if (!$scope.password) {
            $scope.loadingState = 'NOT_OK';
            return;
          }
          $scope.loadingState = 'LOADING';

          createRestCall();
        };

        let profile = null;
        $timeout(function() {
          $scope.$watch(
            'profile',
            value => {
              if (value) {
                profile = value;
                handlePasswordUpdate();
              }
            },
            true
          );
        });

        let sanitize = value => value || '';

        // Wait a second for more user input before validating
        var createRestCall = debounce(function() {
          if (!$scope.password) {
            return;
          }

          passwordPolicyProvider
            .validate(
              {
                password: $scope.password,
                profile: {
                  id: sanitize(profile.id),
                  firstName: sanitize(profile.firstName),
                  lastName: sanitize(profile.lastName),
                  email: sanitize(profile.email)
                }
              },
              function(err, res) {
                if (err) {
                  $scope.loadingState = 'NOT_OK';
                  $scope.tooltip = 'PASSWORD_POLICY_TOOLTIP_ERROR';
                  return;
                }
                if (res.valid) {
                  $scope.loadingState = 'OK';
                  $scope.isValid = true;
                } else {
                  $scope.loadingState = 'NOT_OK';
                  $scope.isValid = false;

                  $scope.tooltip = 'PASSWORD_POLICY_TOOLTIP_PARTIAL';

                  $scope.rules = res.rules;
                }
              }
            )
            .catch(angular.noop);
        }, 1000);

        $scope.$watch('[password]', handlePasswordUpdate, true);
      },
      template: template
    };
  }
];
