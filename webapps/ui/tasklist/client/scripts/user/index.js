'use strict';

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  auth = require('camunda-commons-ui/lib/auth/index'),
  api = require('../api/index'),

  camUserLogoutCtrl = require('./controller/cam-user-logout-ctrl'),
  camUserLoginCtrl = require('./controller/cam-user-login-ctrl');

  /**
   * @module cam.tasklist.user
   */

  /**
   * @memberof cam.tasklist
   */

  var userModule = angular.module('cam.tasklist.user', [
    auth.name,
    api.name
  ]);

  /**
   * controller to be used for the /logout route
   */
  userModule.controller('camUserLogoutCtrl', camUserLogoutCtrl);

  /**
   * Controller used for the /login route
   */
  userModule.controller('camUserLoginCtrl', camUserLoginCtrl);


  module.exports = userModule;

