'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', 'GroupResource', function ($scope, GroupResource) {

    $scope.availableOperations={};

    GroupResource.query().$then(function(response) {
      $scope.groupList = response.data;
    });

    GroupResource.OPTIONS().$then(function(response) {
      angular.forEach(response.data.links, function(link){
        $scope.availableOperations[link.rel] = true;
      });
    });

  }];

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/groups', {
      templateUrl: require.toUrl('./app/admin/pages/groups.html'),
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
      }
    });
  }];

  module
    .config(RouteConfig);

});
