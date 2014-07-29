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
    '$rootScope',
    'AuthenticationService',
    'Notifications',
  function(
    $rootScope,
    AuthenticationService,
    Notifications
  ) {
    AuthenticationService
      .logout()
      .then(function() {
        Notifications.add({
          text: 'You are logged out.'
        });
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
        .then(function(authentication) {
          Notifications.addMessage({
            message: 'You are now logged in.'
          });

          $scope.$parent.$parent.$close(authentication);
        }, function() {
          Notifications.addError({
            message: 'Cannot log in with those credentials.'
          });
        });
    };


    $scope.cancel = function() {
      $scope.$parent.$parent.$dismiss();
    };
  }]);

  return userModule;
});
