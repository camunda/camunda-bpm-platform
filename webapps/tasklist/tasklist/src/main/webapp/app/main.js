'use strict';

define([ "angularModule" ], function(angularModule) {

  var module =
    angularModule("tasklist", [
      "ng",
      "ngResource",
      "ngSanitize",
      "ngCookies",
      "tasklist.pages",
      "tasklist.services",
      "tasklist.directives",
      'common.directives',
      'common.extensions',
      'common.services' ]);

  

  var DefaultController = function($scope, Notifications, Authentication, $location) {

    $scope.auth = Authentication.auth;

    // needed for form validation
    // DO NOT REMOVE FROM DEFAULT CONTROLLER!
    $scope.errorClass = function(form) {
      return form.$valid || !form.$dirty ? "" : "error";
    };

    $scope.$on("responseError", new ResponseErrorHandler(Notifications, Authentication, $location).handlerFn);
  };

  DefaultController.$inject = ["$scope", "Notifications", "Authentication", "$location"];

  var ModuleConfig = function($routeProvider, $locationProvider, $httpProvider) {
    $httpProvider.responseInterceptors.push('httpStatusInterceptor');
    $routeProvider.otherwise({ redirectTo: "/overview" });
  };

  ModuleConfig.$inject = ["$routeProvider", "$locationProvider", "$httpProvider"];

  module
    .config(ModuleConfig)
    .controller("DefaultController", DefaultController);

  return module;
});