'use strict';

var angular = require('angular');
var testModule = require('../../../../common/unit-tests/tests-module');
var externalTasksModule = require('../index');

module.exports = angular.module('cockpit.plugin.external-tasks.process-instance-runtime-tab.tests', [
  testModule.name,
  externalTasksModule.name
]);
