'use strict';


define([
  'angular',
  'camunda-commons-ui/auth',
  'camunda-tasklist-ui/api',

  './directives/cam-auth-login'
], function(
  angular,
  auth,
  api,

  camAuthLogin
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


  userModule.directive('camAuthLogin', camAuthLogin);

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

  return userModule;
});
