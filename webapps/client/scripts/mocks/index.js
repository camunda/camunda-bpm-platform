'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
define([
           'angular',
           'camunda-tasklist-ui/mocks/pile',
           'camunda-tasklist-ui/mocks/task',
           'camunda-tasklist-ui/mocks/user',
           'camunda-tasklist-ui/mocks/session',
           'camunda-tasklist-ui/mocks/process'
], function(angular) {

  /**
   * @module cam.tasklist.mocks
   */

  /**
   * @memberof cam.tasklist
   */

  return angular.module('cam.tasklist.mocks', []);
});
