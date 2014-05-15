'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'camunda-tasklist/mocks/pile', 'camunda-tasklist/mocks/task'
], function(angular) {
  return angular.module('cam.tasklist.mocks', [
    // require('camunda-tasklist/mocks/pile').name,
    // require('camunda-tasklist/mocks/task').name
  ]);
});
