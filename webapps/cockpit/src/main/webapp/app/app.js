'use strict';

angular.module('cockpit', [
    'ng',
    'dashboard',
    'process.definition',
    'cockpit.services',
    'cockpit.directives',
    'cockpit.resources'
    ])

.config(['$routeProvider', '$httpProvider', function($routeProvider,$httpProvider) {

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