'use strict';

var angular = require('angular');
var testModule = require('../../../../unit-tests/tests-module');
var externalTasksModule = require('../index');
var camCommon = require('../../index');

module.exports = angular.module('cam-common.external-tasks-common.tests', [
  testModule.name,
  externalTasksModule.name,
  camCommon.name
]);
