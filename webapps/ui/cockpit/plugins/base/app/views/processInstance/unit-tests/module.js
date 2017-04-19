'use strict';

var angular = require('angular');

var testModule = require('../../../../../../../common/unit-tests/tests-module');
var pagesModule = require('../../main');

module.exports = angular.module('cam.cockpit.pages.tests', [
  testModule.name,
  pagesModule.name
]);
