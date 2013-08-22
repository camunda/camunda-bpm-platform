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

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/users', {
      templateUrl: 'pages/users.html',
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
      }
    });
  }];

  module
    .config(RouteConfig);

});
