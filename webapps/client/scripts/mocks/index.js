'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular',
           'camunda-tasklist/mocks/pile',
           'camunda-tasklist/mocks/task',
           'camunda-tasklist/mocks/session'
], function(angular) {
  return angular.module('cam.tasklist.mocks', []);
});
