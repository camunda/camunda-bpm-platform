'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', 'InitialUserResource', 'Notifications', '$location', 'Uri', function ($scope, InitialUserResource, Notifications, $location, Uri) {

    if (!/.*\/app\/admin\/(\w+)\/setup\/.*/.test($location.absUrl())) {
      $location.path("/");
      return;
    }

    $scope.engineName = Uri.appUri(':engine');

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
        credentials : { password : $scope.credentials.password }
      };

      InitialUserResource.create(user).$then(
        function(){
          // TODO: full page reload necessary?
          window.location = $location.absUrl().split("setup/#")[0];
        },
        function(){
          Notifications.addError({ type:"error", status:"Error", message:"Could not create initial user." });
        }
      );
    }

  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/setup', {
      templateUrl: 'pages/setup.html',
      controller: Controller
    });
    // multi tenacy
    $routeProvider.when('/:engine/setup', {
      templateUrl: 'pages/setup.html',
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});
