/* global define: false */
define(['angular'], function(angular) {
  'use strict';

  var module = angular.module('cockpit.pages');

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

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/dashboard', {
      templateUrl: require.toUrl('./app/cockpit/pages/dashboard.html'),
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser
      }
    });
  }];

  module.config(RouteConfig);
});
