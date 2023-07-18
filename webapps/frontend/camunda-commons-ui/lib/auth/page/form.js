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

const $ = require('jquery');
module.exports = [
  '$scope',
  'AuthenticationService',
  'Notifications',
  '$translate',
  'Views',
  'canonicalAppName',
  function(
    $scope,
    AuthenticationService,
    Notifications,
    $translate,
    views,
    canonicalAppName
  ) {
    $scope.status = 'INIT';

    // ensure focus on username input
    var autofocusField = $('form[name="signinForm"] [autofocus]')[0];
    if (autofocusField) {
      autofocusField.focus();
    }

    const loginDataPlugins = views.getProviders({
      component: `${canonicalAppName}.login.data`
    });

    $scope.login = function() {
      $scope.status = 'LOADING';
      const loginDataPromise = AuthenticationService.login(
        $scope.username,
        $scope.password
      );

      loginDataPlugins.forEach(loginDataPlugin => {
        loginDataPlugin.result &&
          loginDataPlugin.result(loginDataPromise, $scope);
      });

      return loginDataPromise
        .then(function() {
          $scope.status = 'DONE';
          Notifications.clearAll();
          $scope.$root.$broadcast('first-visit-info-box-dismissed');
        })
        .catch(function(error) {
          $scope.status = 'ERROR';
          delete $scope.username;
          delete $scope.password;

          Notifications.addError({
            status: $translate.instant('PAGE_LOGIN_FAILED'),
            message:
              (error.data && error.data.message) ||
              $translate.instant('PAGE_LOGIN_ERROR_MSG'),
            scope: $scope,
            exclusive: true
          });
        });
    };
  }
];
