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

var template = require('./processes.html')();

var angular = require('../../../../../camunda-commons-ui/vendor/angular');

var Controller = [
  '$scope',
  '$location',
  '$timeout',
  'Views',
  'Data',
  'dataDepend',
  'page',
  '$translate',
  function(
    $scope,
    $location,
    $timeout,
    Views,
    Data,
    dataDepend,
    page,
    $translate
  ) {
    var $rootScope = $scope.$root;

    var processData = ($scope.processData = dataDepend.create($scope));

    $scope.dashboardVars = {read: ['processData']};
    $scope.dashboardProviders = Views.getProviders({
      component: 'cockpit.processes.dashboard'
    });

    Data.instantiateProviders('cockpit.dashboard.data', {
      $scope: $scope,
      processData: processData
    });

    // INITIALIZE PLUGINS
    var dashboardPlugins = Views.getProviders({
      component: 'cockpit.processes.dashboard'
    });

    var initData = {
      $scope: $scope,
      processData: processData
    };

    for (var i = 0; i < dashboardPlugins.length; i++) {
      if (typeof dashboardPlugins[i].initialize === 'function') {
        dashboardPlugins[i].initialize(initData);
      }
    }

    var search = $location.search();
    if (search.targetPlugin) {
      $timeout(function() {
        var el = angular.element(
          '[data-plugin-id="' + search.targetPlugin + '"]'
        );
        if (el.length) {
          el[0].scrollIntoView();
        }
      });
    }

    $rootScope.showBreadcrumbs = true;

    page.breadcrumbsClear();

    page.breadcrumbsAdd({
      label: $translate.instant('PROCESS_PROCESSES')
    });

    page.titleSet($translate.instant('PROCESS_PROCESSES'));
  }
];

var RouteConfig = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/processes', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];

module.exports = RouteConfig;
