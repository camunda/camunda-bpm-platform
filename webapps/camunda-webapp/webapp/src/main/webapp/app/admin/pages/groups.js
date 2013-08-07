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
