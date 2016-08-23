'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tasks.html', 'utf8');

var Controller = [
  '$scope', 'Views', 'page',
  function($scope, Views, page) {
    var $rootScope = $scope.$root;

    $rootScope.showBreadcrumbs = true;

    page.breadcrumbsClear();
    page.breadcrumbsAdd({
      label : 'Human Tasks'
    });

    page.titleSet('Human Tasks');

    // INITIALIZE PLUGINS
    $scope.plugins = Views.getProviders({ component : 'cockpit.tasks.dashboard' });
  }
];

var RouteConfig = ['$routeProvider', function($routeProvider) {
  $routeProvider.when('/tasks', {
    template : template,
    controller : Controller,
    authentication : 'required',
    reloadOnSearch : false
  });
}];

module.exports = RouteConfig;
