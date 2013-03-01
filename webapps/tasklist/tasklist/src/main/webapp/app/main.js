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
      'common.directives',
      'common.extensions',
      'common.services' ]);

  var ResponseErrorHandler = function(Errors, Authentication, $location) {

    this.handlerFn = function(event, responseError) {
      var status = responseError.status,
          data = responseError.data;

      switch (status) {
      case 500:
        if (data && data.message) {
          Errors.add({ status: "Error", message:  data.message, type: data.exceptionType });
        } else {
          Errors.add({ status: "Error", message: "A problem occurred: Try to refresh the view or login and out of the application. If the problem persists, contact your administrator." });
        }
        break;
      case 0:
        Errors.add({ status: "Request Timeout", message:  "Your request timed out. Try refreshing the page." });
        break;
      case 401:
        Errors.clear("Unauthorized");
        
        if (Authentication.current()) {
          Errors.add({ status: "Unauthorized", message:  "Your session has expired. Please login again." });
        } else {
          Errors.add({ status: "Unauthorized", message:  "Login is required to access this page." });
        }

        Authentication.set(null);
        $location.path("/login");

        break;
      default:
        Errors.add({ status: "Error", message :  "A problem occurred: Try to refresh the view or login and out of the application. If the problem persists, contact your administrator." });
      }
    };
  };

  var DefaultController = function($scope, Errors, Authentication, $location) {

    $scope.auth = Authentication.auth;

    // needed for form validation
    // DO NOT REMOVE FROM DEFAULT CONTROLLER!
    $scope.errorClass = function(form) {
      return form.$valid || !form.$dirty ? "" : "error";
    };

    $scope.$on("responseError", new ResponseErrorHandler(Errors, Authentication, $location).handlerFn);
  };

  DefaultController.$inject = ["$scope", "Errors", "Authentication", "$location"];

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