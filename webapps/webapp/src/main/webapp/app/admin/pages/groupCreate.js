'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', 'GroupResource', 'Notifications', '$location', function ($scope, GroupResource, Notifications, $location) {

    // data model for new group
    $scope.group = {
      id : "",
      name : "",
      type : ""
    }

    $scope.createGroup = function() {
      var group = $scope.group;
      GroupResource.createGroup(group).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"Successfully created new group "+group.id});
          $location.path("/groups");
        },
        function(){
          Notifications.addError({ status: "Failed", message: "Could not create group " + group.id + ". Check if it already exists." });
        }
      );
    }

  }];

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/group-create', {
      templateUrl: 'pages/groupCreate.html',
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
      }
    });
  }];

  module
    .config(RouteConfig);

});