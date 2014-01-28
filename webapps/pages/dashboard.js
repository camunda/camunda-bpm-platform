'use strict';

define(['angular'], function(angular) {

  var module = angular.module('cockpit.pages');

  var Controller = ['$scope', '$rootScope', 'Views', 'Data', 'dataDepend', function ($scope, $rootScope, Views, Data, dataDepend) {

    var processData = $scope.processData = dataDepend.create($scope);

    $scope.dashboardVars = { read: [ 'processData' ] };
    $scope.dashboardProviders = Views.getProviders({ component: 'cockpit.dashboard'});

    Data.instantiateProviders('cockpit.dashboard.data', {$scope: $scope, processData : processData});

    // reset breadcrumbs
    $rootScope.clearBreadcrumbs();

    // set the page title
    $rootScope.pageTitle = [
      'camunda Cockpit',
      'Dashboard'
    ].join(' | ');
  }];

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/dashboard', {
      templateUrl: 'pages/dashboard.html',
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser
      }
    });
  }];

  module.config(RouteConfig);
});
