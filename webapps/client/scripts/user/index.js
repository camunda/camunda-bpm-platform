'use strict';


define([
  'angular',
  'camunda-commons-ui/auth',
  'camunda-tasklist-ui/api',

  './controller/cam-user-logout-ctrl',
  './controller/cam-user-login-ctrl'
], function(
  angular,
  auth,
  api,

  camUserLogoutCtrl,
  camUserLoginCtrl
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
  ]);

  /**
   * controller to be used for the /logout route
   */
  userModule.controller('camUserLogoutCtrl', camUserLogoutCtrl);

  /**
   * Controller used for the /login route
   */
  userModule.controller('camUserLoginCtrl', camUserLoginCtrl);


  return userModule;
});

