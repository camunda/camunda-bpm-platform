'use strict';

angular.module('cockpit', [
    'ng',
    'dashboard',
    'cockpit.filters',
    'cockpit.services',
    'cockpit.directives'])

.config(['$routeProvider', '$locationProvider', '$httpProvider', 'AppProvider', function($routeProvider, $locationProvider, $httpProvider, AppProvider) {

    $locationProvider.html5Mode(true);
    $httpProvider.responseInterceptors.push('cockpitHttpInterceptor');

    $routeProvider.otherwise({redirectTo: AppProvider.root() + '/dashboard'});
}])

.controller('DefaultController', ['$scope', 'Error', function ($scope, Error) {
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
}]);