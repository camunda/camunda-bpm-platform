'use strict';

define(['angular'], function(angular) {

  var module = angular.module('cockpit.pages');

  var Controller = ['$scope', '$rootScope', 'Views', function ($scope, $rootScope, Views) {

    $scope.dashboardProviders = Views.getProviders({ component: 'cockpit.dashboard'});

    // reset breadcrumbs
    $rootScope.clearBreadcrumbs();

  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/dashboard', {
      templateUrl: 'pages/dashboard.html',
      controller: Controller
    });

    // multi tenacy
    $routeProvider.when('/:engine/dashboard', {
      templateUrl: 'pages/dashboard.html',
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});
