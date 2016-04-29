'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/dashboard.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');


var Controller = [
  '$scope',
  'Views',
  'page',
function (
  $scope,
  Views,
  page
) {
  var $rootScope = $scope.$root;

  $scope.dashboardPlugins = Views.getProviders({
    component: 'admin.dashboard.section'
  });

  $rootScope.showBreadcrumbs = true;

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
