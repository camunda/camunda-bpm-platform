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

var template = require('./tasks.html')();

var Controller = [
  '$scope',
  'Views',
  'page',
  '$translate',
  function($scope, Views, page, $translate) {
    var $rootScope = $scope.$root;

    $rootScope.showBreadcrumbs = true;

    page.breadcrumbsClear();
    page.breadcrumbsAdd({
      label: $translate.instant('TASKS_HUMAN_TASKS')
    });

    page.titleSet($translate.instant('TASKS_HUMAN_TASKS'));

    // INITIALIZE PLUGINS
    $scope.plugins = Views.getProviders({component: 'cockpit.tasks.dashboard'});
  }
];

var RouteConfig = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/tasks', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];

module.exports = RouteConfig;
