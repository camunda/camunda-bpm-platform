'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', 'UserResource', 'Notifications', '$location', function ($scope, UserResource, Notifications, $location) {

    // data model for user profile
    $scope.profile = {
      id : "",
      firstName : "",
      lastName : "",
      email : ""
    }

    // data model for credentials
    $scope.credentials = {
        password : "",
        password2 : ""
    };

    $scope.createUser = function() {
      var user = {
        profile : $scope.profile,
        credentials : { password : $scope.credentials.password}
      }
      UserResource.createUser(user).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"Successfully created new user "+user.profile.id});
          $location.path("/users");
        },
        function(){
          Notifications.addError({type:"error", status:"Error", message:"Could not update user profile."});
        }
      );
    }

  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/users/create', {
      templateUrl: 'pages/user-create.html',
      controller: Controller
    });

    // multi tenacy
    $routeProvider.when('/:engine/users/create', {
      templateUrl: 'pages/user-create.html',
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});
