'use strict';

define(['angular'], function(angular) {

  var module = angular.module('cockpit.pages');

  var Controller = ['$scope', '$rootScope', 'Views', function ($scope, $rootScope, Views) {

    $scope.dashboardProviders = Views.getProviders({ component: 'cockpit.dashboard'});

    // reset breadcrumbs
    $rootScope.clearBreadcrumbs();
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
