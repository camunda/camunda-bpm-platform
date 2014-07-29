'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }

define([
  'angular',
  'camunda-commons-ui/auth',
  'camunda-tasklist-ui/api',

  'camunda-commons-ui/directives/notificationsPanel',

  'text!camunda-tasklist-ui/user/login.html'
], function(
  angular,
  auth,
  api,
  notificationsPanel
) {

  /**
   * @module cam.tasklist.user
   */

  /**
   * @memberof cam.tasklist
   */

  var userModule = angular.module('cam.tasklist.user', [
    auth.name,
    api.name,
    'ui.bootstrap',
    'cam.form'
  ]);

  userModule.directive('notificationsPanel', notificationsPanel);

  /**
   * Controller used for the /login route
   */
  userModule.controller('userLoginCtrl', [
    '$location',
    '$modal',
    'AuthenticationService',
  function(
    $location,
    $modal
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
    'Notifications',
    'Uri',
  function(
    $window,
    $rootScope,
    $cacheFactory,
    AuthenticationService,
    Notifications,
    Uri
  ) {
    AuthenticationService
      .logout()
      .then(function(success) {
        if (success) {
          Notifications.add({
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
    'Notifications',
  function(
    $scope,
    $rootScope,
    $location,
    AuthenticationService,
    Notifications
  ) {
    $scope.submitForm = function(htmlForm) {
      return htmlForm.$valid;
    };

    console.info('Notifications', Notifications);

    // /camunda/api/admin/auth/user/default/login/cockpit
    $scope.ok = function() {
      AuthenticationService
        .login($scope.username, $scope.password)
        .then(function(success) {

          if (success) {
            Notifications.addMessage({
              message: 'You are now logged in.'
            });

            $scope.user = $rootScope.authentication.user;

            $rootScope.$broadcast('loggedin', $rootScope.authentication.user);

            $scope.$parent.$parent.$close($scope.user.name);
          }
          else {
            Notifications.addError({
              message: 'Cannot log in with those credentials.'
            });
          }
        });
    };


    $scope.cancel = function() {
      $scope.$parent.$parent.$dismiss();
    };
  }]);

  return userModule;
});
