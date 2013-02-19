'use strict';

angular.module('cockpit', [
    'ng',
    'dashboard',
    'cockpit.services',
    'cockpit.directives',
    'cockpit.resources'
    ])

.config(['$routeProvider', '$locationProvider', '$httpProvider', function($routeProvider, $locationProvider, $httpProvider) {

    $httpProvider.responseInterceptors.push('cockpitHttpInterceptor');

    $routeProvider.otherwise({redirectTo: '/dashboard'});
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