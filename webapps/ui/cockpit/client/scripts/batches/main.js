'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    dataDepend = require('angular-data-depend'),
    camCommons = require('camunda-commons-ui/lib/index'),

    routes = require('./config/routes');

  var ngDeps = [
    'cam.commons',
    'dataDepend',
    'ngRoute'
  ];

  var batchModule = angular.module('cam.cockpit.batch', ngDeps);

  batchModule.config(routes);

  module.exports = batchModule;
