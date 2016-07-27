'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/dashboard.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');
var isArray = angular.isArray;

var Controller = [
  '$scope',
  'Views',
  'page',
  '$injector',
  function(
  $scope,
  Views,
  page,
  $injector
) {
    var $rootScope = $scope.$root;

    $scope.dashboardPlugins = Views.getProviders({
      component: 'admin.dashboard.section'
    })
  .map(function(plugin) {
    if (isArray(plugin.access)) {
      var fn = $injector.invoke(plugin.access);

      fn(function(err, access) {
        if (err) { throw err; }

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

    page.titleSet('Dashboard');
  }];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];
