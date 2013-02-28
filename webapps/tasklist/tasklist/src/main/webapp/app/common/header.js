"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var LogoutController = function($scope, $location, Authentication) {

    $scope.logout = function() {
      Authentication.logout().then(function(success) {
        $location.path("");
      });
    };
  };

  LogoutController.$inject = ["$scope", "$location", "Authentication"];

  module
    .controller("LogoutController", LogoutController);
});