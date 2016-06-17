'use strict';

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/reports-view.html', 'utf8');
var angular = require('camunda-commons-ui/vendor/angular');
var extend = angular.extend;

var Controller = [
  '$scope',
  '$route',
  'page',
  'dataDepend',
  'Views',
  function(
  $scope,
  $route,
  page,
  dataDepend,
  Views
) {
    $scope.selectedReportId = (($route.current || {}).params || {}).reportType || null;

  // utilities ///////////////////////////////////////////////////////////////////

    function getPluginProviders(options) {
      var _options = extend({}, options || {}, { component: 'cockpit.report' });
      return Views.getProviders(_options);
    }

    var getDefaultReport = function(reports) {
      if (!reports || !$scope.selectedReportId) {
        return;
      }

      if ($scope.selectedReportId) {
        return (getPluginProviders({ id: $scope.selectedReportId }) || [])[0];
      }
    };

  // breadcrumb //////////////////////////////////////////////////////////////

    $scope.$root.showBreadcrumbs = true;

    page.breadcrumbsClear();

    if ($scope.selectedReportId) {
      var reportTypePlugin = getPluginProviders({ id: $scope.selectedReportId });

      $scope.pluginLabel = reportTypePlugin[0].label;

      if (reportTypePlugin.length) {
        page.breadcrumbsAdd([
          {
            label: 'Reports',
            href: '#/reports'
          },
          {
            label: reportTypePlugin[0].label
          }
        ]);

        page.titleSet(reportTypePlugin[0].label + ' report');
      }
    }
    else {
      page.breadcrumbsAdd({
        label: 'Reports'
      });

      page.titleSet('Reports');
    }



  // provide data ///////////////////////////////////////////////////////////

    var reportData = $scope.reportData = dataDepend.create($scope);

    var plugins = getPluginProviders();
    var plugin = getDefaultReport(plugins);

    reportData.provide('plugins', plugins);
    reportData.provide('plugin', plugin);

    $scope.getPluginProviders = getPluginProviders;

    $scope.$on('$routeChanged', function() {
      var _plugin = getDefaultReport(plugins);
      reportData.set('plugin', _plugin);
    });

    $scope.reportTitle = (($scope.getPluginProviders() || [])[0] || {}).label || null;
  }];

var RouteConfig = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/reports/:reportType?', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];

module.exports = RouteConfig;
