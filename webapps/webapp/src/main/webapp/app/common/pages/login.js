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
    'page',
  function (
    $scope,
    Authentication,
    AuthenticationService,
    Notifications,
    $location,
    page
  ) {

    if (Authentication.username()) {
      $location.path('/');
    }

    page.titleSet('camunda | login');

    $scope.login = function () {
      // possible bug to investigate (when password is remembered, under FF)
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
