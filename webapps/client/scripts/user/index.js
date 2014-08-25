'use strict';


define([
  'angular',
  'camunda-commons-ui/auth',
  'camunda-tasklist-ui/api',

  'text!camunda-tasklist-ui/user/login.html'
], function(
  angular,
  auth,
  api
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

  /**
   * Controller used for the /login route
   */
  userModule.controller('userLoginCtrl', [
    '$modal',
  function(
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
    '$translate',
    'AuthenticationService',
    'Notifications',
  function(
    $translate,
    AuthenticationService,
    Notifications
  ) {
    AuthenticationService
      .logout()
      .then(function() {
        $translate('LOGGED_OUT').then(function(translated) {
          Notifications.add({
            status: translated
          });
        });
      });
  }]);


  userModule.controller('userLoginModalFormCtrl', [
    '$scope',
    '$translate',
    'AuthenticationService',
    'Notifications',
  function(
    $scope,
    $translate,
    AuthenticationService,
    Notifications
  ) {
    $scope.submitForm = function(htmlForm) {
      return htmlForm.$valid;
    };


    // /camunda/api/admin/auth/user/default/login/cockpit
    $scope.ok = function() {
      function success() {
        $translate('LOGGED_IN').then(function(translated) {
          Notifications.addMessage({
            duration: 5000,
            status: translated
          });
        });

        $scope.$parent.$parent.$close();
      }


      function error() {
        $translate('CREDENTIALS_ERROR').then(function(translated) {
          Notifications.addError({
            status: translated
          });
        });
      }


      AuthenticationService
        .login($scope.username, $scope.password)
        .then(success, error);
    };


    $scope.cancel = function() {
      $scope.$parent.$parent.$dismiss();
    };
  }]);

  return userModule;
});
