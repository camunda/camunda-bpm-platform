'use strict';

var angular = require('camunda-bpm-sdk-js/vendor/angular'),

   transform = require('./transform'),
   variables = require('./variables'),
   page = require('./page'),
   breadcrumbTrails = require('./breadcrumbTrails'),
   routeUtil = require('./routeUtil'),
   camAPI = require('./cam-api');

  var servicesModule = angular.module('cam.cockpit.services', []);

  servicesModule.factory('Transform', transform);
  servicesModule.factory('Variables', variables);
  servicesModule.service('page', page);
  servicesModule.factory('breadcrumbTrails', breadcrumbTrails);
  servicesModule.factory('routeUtil', routeUtil);
  servicesModule.factory('camAPI', camAPI);

  module.exports = servicesModule;
