'use strict';

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/reports-view.html', 'utf8');
var angular = require('angular');
var extend = angular.extend;

var Controller = [
  '$scope',
  'search',
  'page',
  'dataDepend',
  'camAPI',
  'Views',
function(
  $scope,
  search,
  page,
  dataDepend,
  camAPI,
  Views
) {

  // utilities ///////////////////////////////////////////////////////////////////

  function getPluginProviders(options) {
    var _options = extend({}, options || {}, { component: 'cockpit.report' });
    return Views.getProviders(_options);
  }

  var updateSilently = function (params) {
    search.updateSilently(params);
  };

  var getPropertyFromLocation = function (property) {
    var _search = search() || {};
    return _search[property] || null;
  };

  var getDefaultReport = function(reports) {
    if (!reports || !reports.length) {
      return;
    }

    var selectedReport = getPropertyFromLocation('report');

    if (selectedReport) {
      var _plugin = (getPluginProviders({ id: selectedReport }) || [])[0];
      if (_plugin && reports.indexOf(_plugin) != -1) {
        return _plugin;
      }
    }

    search.updateSilently({
      report: null
    });

    return reports[0];
  };

  // breadcrumb //////////////////////////////////////////////////////////////

  page.titleSet([
    'Camunda Cockpit',
    'Reports'
  ].join(' | '));

  page.breadcrumbsClear();

  $scope.$root.showBreadcrumbs = true;

  page.breadcrumbsAdd({
    label: 'Reports'
  });

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
  $routeProvider.when('/reports', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];

module.exports = RouteConfig;
