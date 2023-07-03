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

var template = require('./system.html?raw');

var angular = require('angular');

var Controller = [
  '$scope',
  'page',
  '$location',
  '$routeParams',
  'Views',
  '$translate',
  '$injector',
  function(
    $scope,
    page,
    $location,
    $routeParams,
    Views,
    $translate,
    $injector
  ) {
    $scope.$root.showBreadcrumbs = true;

    page.titleSet($translate.instant('SYSTEM_SYSTEM_SETTINGS'));

    page.breadcrumbsClear();

    page.breadcrumbsAdd([
      {
        label: $translate.instant('SYSTEM_SYSTEM_SETTINGS'),
        href: '#/system'
      }
    ]);

    $scope.systemSettingsProviders = Views.getProviders({
      component: 'admin.system'
    }).map(function(plugin) {
      if (angular.isArray(plugin.access)) {
        var fn = $injector.invoke(plugin.access);

        fn(function(err, access) {
          if (err) {
            throw err;
          }

          plugin.accessible = access;
        });
      } else {
        plugin.accessible = true;
      }

      return plugin;
    });

    var selectedProviderId = $routeParams.section;
    if (selectedProviderId) {
      $scope.activeSettingsProvier = Views.getProviders({
        component: 'admin.system',
        id: $routeParams.section
      })[0];
    }

    $scope.activeClass = function(link) {
      var path = $location.absUrl();
      return path.indexOf(link) != -1 ? 'active' : '';
    };
  }
];

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/system', {
      template: template,
      controller: Controller,
      authentication: 'required'
    });
  }
];
