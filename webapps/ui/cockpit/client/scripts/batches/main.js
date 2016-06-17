'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

require('angular-data-depend');
require('camunda-commons-ui/lib/index');

var routes = require('./config/routes');

var ngDeps = [
  'cam.commons',
  'dataDepend',
  'ngRoute'
];

var batchModule = angular.module('cam.cockpit.batch', ngDeps);

batchModule.config(routes);

module.exports = batchModule;
