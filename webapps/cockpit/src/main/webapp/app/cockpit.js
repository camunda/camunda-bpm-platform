'use strict';

define([ "angularModule" ], function(angularModule) {

  var module =
    angularModule("cockpit", [
      "ng",
      "ngResource",
      
      'common.directives',
      'common.extensions',
      'common.resources',
      'common.services',
      
      'cockpit.pages',
      'cockpit.directives',
      'cockpit.resources']);


  var Controller = function ($scope, Error) {
    
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
  
  Controller.$inject = ["$scope", "Error"];
  
  var ModuleConfig = function($routeProvider, $httpProvider) {
    $httpProvider.responseInterceptors.push('httpStatusInterceptor');
    $routeProvider.otherwise({ redirectTo: "/dashboard" });
  };

  ModuleConfig.$inject = ["$routeProvider", "$httpProvider"];

  module
    .config(ModuleConfig)
    .controller('DefaultCtrl', Controller);
  
  return module;

});
