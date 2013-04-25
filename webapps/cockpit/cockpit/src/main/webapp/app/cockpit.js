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


  var Controller = function ($scope, Errors) {
    
    $scope.appErrors = function () {
      return Errors.errors;
    };
    
    $scope.removeError = function (error) {
      Errors.clear(error);
    };
    
    // needed for form validation
    // DO NOT REMOVE FROM DEFAULT CONTROLLER!
    $scope.errorClass = function(form) {
      return form.$valid || !form.$dirty ? '' : 'error';
    };
    
  };
  
  Controller.$inject = ["$scope", "Errors"];
  
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
