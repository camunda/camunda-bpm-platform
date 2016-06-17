'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decisions.html', 'utf8');

var Controller = [
  '$scope',
  'Views',
  'page',
  function(
  $scope,
  Views,
  page
) {
    var $rootScope = $scope.$root;

    $rootScope.showBreadcrumbs = true;

    page.breadcrumbsClear();

    page.breadcrumbsAdd({
      label: 'Decisions'
    });

    page.titleSet('Decisions');

  // INITIALIZE PLUGINS
    $scope.plugins = Views.getProviders({ component: 'cockpit.decisions.dashboard' });
  }];

var RouteConfig = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/decisions', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];

module.exports = RouteConfig;
