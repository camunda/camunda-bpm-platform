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

  var ResponseErrorHandler = function(Notifications, Authentication, $location) {

    this.handlerFn = function(event, responseError) {
      var status = responseError.status,
          data = responseError.data;

      Notifications.clear({ type: "error" });

      switch (status) {
      case 500:
        if (data && data.message) {
          Notifications.addError({ status: "Error", message: data.message, exceptionType: data.exceptionType });
        } else {
          Notifications.addError({ status: "Error", message: "A problem occurred: Try to refresh the view or login and out of the application. If the problem persists, contact your administrator." });
        }
        break;
      case 0:
        Notifications.addError({ status: "Request Timeout", message:  "Your request timed out. Try refreshing the page." });
        break;
      case 401:
        if (Authentication.current()) {
          Notifications.addError({ status: "Unauthorized", message:  "Your session has expired. Please login again." });
        } else {
          Notifications.addError({ status: "Unauthorized", message:  "Login is required to access this page." });
        }

        Authentication.set(null);
        $location.path("/login");

        break;
      default:
        Notifications.addError({ status: "Error", message :  "A problem occurred: Try to refresh the view or login and out of the application. If the problem persists, contact your administrator." });
      }
    };
  };

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