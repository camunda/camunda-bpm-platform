'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
define([
           'angular',
           './pile',
           './task',
           './user',
           './session',
           './process'
], function(angular) {

  /**
   * @module cam.tasklist.mocks
   */

  /**
   * @memberof cam.tasklist
   */

  return angular.module('cam.tasklist.mocks', []);
});
