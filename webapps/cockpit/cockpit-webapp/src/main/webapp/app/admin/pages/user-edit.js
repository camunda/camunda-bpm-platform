'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', '$routeParams', 'UserResource', 'Notifications', '$location', function ($scope, $routeParams, UserResource, Notifications, $location) {

    $scope.action = $routeParams.action;

    $scope.credentials = {
      password : "", 
      password2 : ""
    };

    var loadProfile = $scope.loadProfile = function() {
      UserResource.profile({userId : $routeParams.userId}).$then(function(response) {
        $scope.user = response.data;
        $scope.profile = angular.copy(response.data);
        $scope.profileCopy = angular.copy(response.data);
      });
    }

    $scope.show = function(fragment) {
      return fragment == $scope.action;
    };

    $scope.activeClass = function(link) {
      var path = $location.absUrl();      
      return path.indexOf(link) != -1 ? "active" : "";
    };

    $scope.updateProfile = function() {      

      UserResource.updateProfile($scope.profile).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"User profile successfully updated."});
          loadProfile();
        },
        function(){
          Notifications.addError({type:"error", status:"Error", message:"Could not update user profile."});
        }
      );
    }

    $scope.updateCredentials = function() {      

      UserResource.updateProfile($scope.profile).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"User profile successfully updated."});
          loadProfile();
        },
        function(){
          Notifications.addError({type:"error", status:"Error", message:"Could not update user profile."});
        }
      );
    }

    /** form must be valid & user must have made some changes */
    var canSubmit = $scope.canSubmit = function(form, modelObject) {
      return form.$valid 
        && (modelObject == null || !angular.equals($scope[modelObject], $scope[modelObject+'Copy']));
    };

    // make sure profile is always loaded
    loadProfile();

  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/users/:userId/:action', {
      templateUrl: 'pages/user-edit.html',
      controller: Controller
    });

    // multi tenacy
    $routeProvider.when('/:engine/users/:userId/:action', {
      templateUrl: 'pages/user-edit.html',
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});
