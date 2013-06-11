'use strict';

define(['angular'], function(angular) {

  var module = angular.module('cockpit.pages');

  var Controller = function ($scope, ProcessDefinitionResource, Views) {

    $scope.dashboardProviders = Views.getProviders({ component: 'cockpit.dashboard'});

  };

  Controller.$inject = ['$scope', 'ProcessDefinitionResource', 'Views'];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when('/dashboard', {
      templateUrl: 'pages/dashboard.html',
      controller: 'DashboardCtrl'
    });
  };

  RouteConfig.$inject = ['$routeProvider'];

  module
    .config(RouteConfig)
    .controller('DashboardCtrl', Controller);

});
