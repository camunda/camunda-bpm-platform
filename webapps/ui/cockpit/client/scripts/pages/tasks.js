'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tasks.html', 'utf8');

var Controller = [
  '$scope', 'Views', 'page', '$translate',
  function($scope, Views, page, $translate) {
    var $rootScope = $scope.$root;

    $rootScope.showBreadcrumbs = true;

    page.breadcrumbsClear();
    page.breadcrumbsAdd({
      label : $translate.instant('TASKS_HUMAN_TASKS')
    });

    page.titleSet($translate.instant('TASKS_HUMAN_TASKS'));

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
