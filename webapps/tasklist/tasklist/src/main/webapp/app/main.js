'use strict';

define([ "angularModule" ], function(angularModule) {

  var module =
    angularModule("tasklist", [
      "ng",
      "tasklist.pages",
      'common.directives',
      'common.extensions',
      'common.resources',
      'common.services' ]);

  var DefaultController = function($scope, Error) {
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
  };

  DefaultController.$inject = ['$scope', 'Error'];

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