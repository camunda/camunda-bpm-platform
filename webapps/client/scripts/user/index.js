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
    '$translate',
    'AuthenticationService',
    'Notifications',
  function(
    $scope,
    $rootScope,
    $location,
    $translate,
    AuthenticationService,
    Notifications
  ) {
    $scope.submitForm = function(htmlForm) {
      return htmlForm.$valid;
    };


    // /camunda/api/admin/auth/user/default/login/cockpit
    $scope.ok = function() {
      AuthenticationService
        .login($scope.username, $scope.password)
        .then(function(success) {

          if (success) {
            $translate('LOGGED_IN').then(function(translated) {
              Notifications.addMessage({
                message: translated
              });
            });

            $scope.user = $rootScope.authentication.user;

            $rootScope.$broadcast('loggedin', $rootScope.authentication.user);

            $scope.$parent.$parent.$close($scope.user.name);
          }
          else {
            $translate('CREDENTIALS_ERROR').then(function(translated) {
              Notifications.addError({
                message: translated
              });
            }, function() {
              throw new Error('Look at the bright side of life...');
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
