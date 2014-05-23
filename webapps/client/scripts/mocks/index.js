'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
define([
           'angular',
           'camunda-tasklist/mocks/pile',
           'camunda-tasklist/mocks/task',
           'camunda-tasklist/mocks/user',
           'camunda-tasklist/mocks/session',
           'camunda-tasklist/mocks/process'
], function(angular) {

  /**
   * @module cam.tasklist.mocks
   */

  /**
   * @memberof cam.tasklist
   */

  return angular.module('cam.tasklist.mocks', []);
});
