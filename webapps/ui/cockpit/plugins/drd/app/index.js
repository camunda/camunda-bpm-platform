'use strict';

var angular = require('camunda-commons-ui/vendor/angular');
var common = require('cam-common');
var dashboard = require('./dashboard');

module.exports = angular.module('cockpit.plugin.drd', [
  common.name,
  dashboard.name
]).name;
