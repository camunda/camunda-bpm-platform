'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', 'UserResource', function ($scope, UserResource) {

    $scope.availableOperations={};

    UserResource.query().$then(function(response) {
      $scope.userList = response.data;
    });

    UserResource.OPTIONS().$then(function(response) {
      angular.forEach(response.data.links, function(link){
        $scope.availableOperations[link.rel] = true;
      });    
    });

  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/users', {
      templateUrl: 'pages/users.html',
      controller: Controller
    });

    // multi tenacy
    $routeProvider.when('/:engine/users', {
      templateUrl: 'pages/users.html',
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});
