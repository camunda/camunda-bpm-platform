'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
  'require',
  'angular',
  'moment',
  'jquery',
  'camunda-commons-ui/auth',
  'camunda-tasklist-ui/api',
  'text!camunda-tasklist-ui/user/login.html'
], function(
  require,
  angular,
  moment,
  $
) {

  /**
   * @module cam.tasklist.user
   */

  /**
   * @memberof cam.tasklist
   */

  var userModule = angular.module('cam.tasklist.user', [
    require('camunda-tasklist-ui/api').name,
    require('camunda-commons-ui/auth').name,
    'ui.bootstrap',
    'cam.form'
  ]);

  /**
   * Redirects an authenticated user to its destination.
   * Used in userLoginModalFormCtrl and userLoginCtrl
   */
  function redirect($location) {
    var search = $location.search();
    if (search) {
      var destination = decodeURIComponent(search.destination);
      if (destination) {
        return $location.url(destination);
      }
    }

    $location.url('/');
  }

  /**
   * Controller used for the /login route
   */
  userModule.controller('userLoginCtrl', [
    '$location',
    '$modal',
    'AuthenticationService',
  function(
    $location,
    $modal,
    AuthenticationService
  ) {
    $modal.open({
      windowClass:  'user-login',
      template:     require('text!camunda-tasklist-ui/user/login.html')
    });
  }]);

  /**
   * controller to be used for the /logout route
   */
  userModule.controller('userLogoutCtrl', [
    '$window',
    '$rootScope',
    '$cacheFactory',
    'AuthenticationService',
    'camTasklistNotifier',
    'Uri',
  function(
    $window,
    $rootScope,
    $cacheFactory,
    AuthenticationService,
    camTasklistNotifier,
    Uri
  ) {
    AuthenticationService
      .logout()
      .then(function(success) {
        if (success) {
          camTasklistNotifier.add({
            text: 'You are logged out.'
          });

          // we make sure none of the request are kept in the cache
          $cacheFactory.get('$http').removeAll();

          // trigger something for the others
          // (although it might not make much sense when doing a full page reload)
          $rootScope.$broadcast('loggedout');

          // for now, it is important not to redirect to "/" but "/login"
          // in order to trigger a full reloading of the app
          $window.location.href = Uri.appUri('app://#/login');
        }
      });
  }]);


  userModule.controller('userLoginModalFormCtrl', [
    '$scope',
    '$rootScope',
    '$location',
    'AuthenticationService',
    'camTasklistNotifier',
  function(
    $scope,
    $rootScope,
    $location,
    AuthenticationService,
    camTasklistNotifier
  ) {
    $scope.submitForm = function(htmlForm) {
      return htmlForm.$valid;
    };

    // /camunda/api/admin/auth/user/default/login/cockpit
    $scope.ok = function() {
      var successMessage = {
        text: 'You are now logged in.'
      };

      var errorMessage = {
        type: 'error',
        text: 'Can not log in with those credentials.'
      };

      AuthenticationService
        .login($scope.username, $scope.password)
        .then(function(success) {

          camTasklistNotifier.add(success ? successMessage : errorMessage);

          if (success) {
            $scope.user = $rootScope.authentication.user;

            $rootScope.$broadcast('loggedin', $rootScope.authentication.user);

            $scope.$parent.$parent.$close($scope.user.name);

            redirect($location);
          }
        });
    };


    $scope.cancel = function() {
      $scope.$parent.$parent.$dismiss();
    };
  }]);

  return userModule;
});
