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

var template = require('./dashboard.html?raw');

var angular = require('camunda-commons-ui/vendor/angular');
var isArray = angular.isArray;

var Controller = [
  '$scope',
  'Views',
  'page',
  '$injector',
  '$translate',
  function($scope, Views, page, $injector, $translate) {
    var $rootScope = $scope.$root;

    $scope.dashboardPlugins = Views.getProviders({
      component: 'admin.dashboard.section'
    }).map(function(plugin) {
      if (isArray(plugin.access)) {
        var fn = $injector.invoke(plugin.access);

        fn(function(err, access) {
          if (err) {
            throw err;
          }

          plugin.accessible = access;
        });
      }

      // accessible by default in case there's no callback
      else {
        plugin.accessible = true;
      }

      return plugin;
    });

    $rootScope.showBreadcrumbs = false;

    page.breadcrumbsClear();

    page.titleSet($translate.instant('DASHBOARD_DASHBOARD'));
  }
];

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];
