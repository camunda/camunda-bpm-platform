'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', function ($scope) {


  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/groups', {
      templateUrl: 'pages/groups.html',
      controller: Controller
    });

    // multi tenacy
    $routeProvider.when('/:engine/groups', {
      templateUrl: 'pages/groups.html',
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});
