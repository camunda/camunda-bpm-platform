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

var template = require('./reports-view.html?raw');
var angular = require('camunda-commons-ui/vendor/angular');
var extend = angular.extend;

var Controller = [
  '$scope',
  '$route',
  'page',
  'dataDepend',
  'Views',
  '$translate',
  function($scope, $route, page, dataDepend, Views, $translate) {
    $scope.selectedReportId =
      (($route.current || {}).params || {}).reportType || null;

    // utilities ///////////////////////////////////////////////////////////////////

    function getPluginProviders(options) {
      var _options = extend({}, options || {}, {component: 'cockpit.report'});
      return Views.getProviders(_options);
    }

    var getDefaultReport = function(reports) {
      if (!reports || !$scope.selectedReportId) {
        return;
      }

      if ($scope.selectedReportId) {
        return (getPluginProviders({id: $scope.selectedReportId}) || [])[0];
      }
    };

    // breadcrumb //////////////////////////////////////////////////////////////

    $scope.$root.showBreadcrumbs = true;

    page.breadcrumbsClear();

    if ($scope.selectedReportId) {
      var reportTypePlugin = getPluginProviders({id: $scope.selectedReportId});

      $scope.pluginLabel = reportTypePlugin[0].label;

      if (reportTypePlugin.length) {
        page.breadcrumbsAdd([
          {
            label: $translate.instant('REPORTS_VIEW_BREAD_CRUMB'),
            href: '#/reports'
          },
          {
            label: reportTypePlugin[0].label
          }
        ]);

        page.titleSet(
          $translate.instant('REPORTS_VIEW_TITLE_SET', {
            name: $translate.instant(reportTypePlugin[0].label)
          })
        );
      }
    } else {
      page.breadcrumbsAdd({
        label: $translate.instant('REPORTS_VIEW_BREAD_CRUMB')
      });

      page.titleSet($translate.instant('REPORTS_VIEW_BREAD_CRUMB'));
    }

    // provide data ///////////////////////////////////////////////////////////

    var reportData = ($scope.reportData = dataDepend.create($scope));

    var plugins = getPluginProviders();
    var plugin = getDefaultReport(plugins);

    reportData.provide('plugins', plugins);
    reportData.provide('plugin', plugin);

    $scope.getPluginProviders = getPluginProviders;

    $scope.$on('$routeChanged', function() {
      var _plugin = getDefaultReport(plugins);
      reportData.set('plugin', _plugin);
    });

    $scope.reportTitle =
      (($scope.getPluginProviders() || [])[0] || {}).label || null;
  }
];

var RouteConfig = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/reports/:reportType?', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];

module.exports = RouteConfig;
