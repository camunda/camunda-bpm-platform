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

var template = require('./welcome.html?raw');

var RouteConfig = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/welcome', {
      template: template,
      // controller: Controller,
      controller: [
        '$scope',
        'Views',
        'Uri',
        function($scope, Views, Uri) {
          var auth = $scope.$root.authentication;

          $scope.canAccessApp = function(appName) {
            return auth.authorizedApps.indexOf(appName) > -1;
          };

          $scope.columnWidth = function() {
            return 12 / (auth.authorizedApps.length - 1);
          };

          $scope.profilePlugins = Views.getProviders({
            component: 'welcome.profile'
          });

          $scope.plugins = Views.getProviders({
            component: 'welcome.dashboard'
          });

          $scope.currentEngine = Uri.appUri(':engine');
        }
      ],
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];

module.exports = RouteConfig;
