'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'camunda-tasklist-ui/session/data'
], function(angular) {

  /**
   * @module cam.tasklist.session
   */

  /**
   * @memberof cam.tasklist
   */

  var sessionModule = angular.module('cam.tasklist.session', [
    'cam.tasklist.session.data'
  ]);

  return sessionModule;
});
