'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/processes.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

  var $ = angular.element;

  var Controller = [
    '$scope',
    '$rootScope',
    'Views',
    'Data',
    'dataDepend',
    'page',
  function (
    $scope,
    $rootScope,
    Views,
    Data,
    dataDepend,
    page
  ) {

    var processData = $scope.processData = dataDepend.create($scope);

    $scope.dashboardVars = { read: [ 'processData' ] };
    $scope.dashboardProviders = Views.getProviders({ component: 'cockpit.processes.dashboard'});

    Data.instantiateProviders('cockpit.dashboard.data', {$scope: $scope, processData : processData});

    // INITIALIZE PLUGINS
    var dashboardPlugins = Views.getProviders({ component: 'cockpit.processes.dashboard' });

    var initData = {
      $scope      : $scope,
      processData : processData
    };

    for(var i = 0; i < dashboardPlugins.length; i++) {
      if(typeof dashboardPlugins[i].initialize === 'function') {
         dashboardPlugins[i].initialize(initData);
      }
    }

    $rootScope.showBreadcrumbs = true;

    page.breadcrumbsClear();

    page.breadcrumbsAdd({
      label: 'Processes'
    });

    page.titleSet('Processes');
  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/processes', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }];

  module.exports = RouteConfig;
