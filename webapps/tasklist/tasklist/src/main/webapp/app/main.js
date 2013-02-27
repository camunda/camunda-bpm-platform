'use strict';

define([ "angularModule" ], function(angularModule) {

  var module =
    angularModule("tasklist", [
      "ng",
      "ngResource",
      "tasklist.pages",
      "tasklist.services",
      'common.directives',
      'common.extensions',
      'common.resources',
      'common.services' ]);

  var DefaultController = function($scope, Error, $location) {
    $scope.appErrors = function () {
      return Error.errors;
    };

    $scope.removeError = function (error) {
      Error.removeError(error);
    };

    // needed for form validation
    // DO NOT REMOVE FROM DEFAULT CONTROLLER!
    $scope.errorClass = function(form) {
      return form.$valid || !form.$dirty ? '' : 'error';
    };

    $scope.$on("response-error", function(event, responseError) {
      console.log(responseError);
      $location.path("/login");
    });
  };

  DefaultController.$inject = ['$scope', 'Error', '$location'];

  var ModuleConfig = function($routeProvider, $locationProvider, $httpProvider) {
    $httpProvider.responseInterceptors.push('httpStatusInterceptor');
    $routeProvider.otherwise({ redirectTo: "/overview" });
  };

  ModuleConfig.$inject = ["$routeProvider", "$locationProvider", "$httpProvider"];

  module
    .config(ModuleConfig)
    .controller('DefaultController', DefaultController);

  return module;
});