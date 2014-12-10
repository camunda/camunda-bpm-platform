/* global define: false */
define(['angular', 'text!./dashboard.html'], function(angular, template) {
  'use strict';

  var Controller = ['$scope', '$rootScope', 'Views', 'Data', 'dataDepend', 'page', function ($scope, $rootScope, Views, Data, dataDepend, page) {

    var processData = $scope.processData = dataDepend.create($scope);

    $scope.dashboardVars = { read: [ 'processData' ] };
    $scope.dashboardProviders = Views.getProviders({ component: 'cockpit.dashboard'});

    Data.instantiateProviders('cockpit.dashboard.data', {$scope: $scope, processData : processData});

    // reset breadcrumbs
    page.breadcrumbsClear();

    page.titleSet([
      'camunda Cockpit',
      'Dashboard'
    ].join(' | '));
  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/dashboard', {
      template: template,
      controller: Controller,
      authentication: 'required'
    });
  }];

  return RouteConfig;
});
