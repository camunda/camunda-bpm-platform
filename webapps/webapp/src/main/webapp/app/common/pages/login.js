/* global ngDefine: false */
ngDefine('camunda.common.pages.login', [ 'angular', 'require', 'module:camunda.common.services.authentication:camunda-common/services/Authentication' ],
  function(module, angular, require) {
  'use strict';

  var Controller = [
    '$scope',
    'Authentication',
    'AuthenticationService',
    'Notifications',
    '$location',
  function (
    $scope,
    Authentication,
    AuthenticationService,
    Notifications,
    $location
  ) {

    if (Authentication.username()) {
      $location.path('/');
    }

    $scope.login = function () {
      // this corrects a bug with password managers (observed in FF at least)
      $scope.username = $scope.username || angular.element('[ng-model="username"]').val();
      $scope.password = $scope.password || angular.element('[ng-model="password"]').val();

      AuthenticationService
        .login($scope.username, $scope.password)
        .then(function(success) {
          Notifications.clearAll();

          if (success) {
            $location.path('/');
          } else {
            Notifications.addError({ status: 'Login Failed', message: 'Wrong credentials or missing access rights to application' });
          }
        });
    };
  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/login', {
      templateUrl: require.toUrl('./login.html'),
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});
