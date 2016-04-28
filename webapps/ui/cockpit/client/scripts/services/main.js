'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

   transform = require('./transform'),
   variables = require('./variables'),
   breadcrumbTrails = require('./breadcrumbTrails'),
   routeUtil = require('./../../../../common/scripts/services/routeUtil'),
   page = require('./../../../../common/scripts/services/page'),
   camAPI = require('./../../../../common/scripts/services/cam-api');

  var servicesModule = angular.module('cam.cockpit.services', []);

  servicesModule.factory('Transform', transform);
  servicesModule.factory('Variables', variables);
  servicesModule.service('page', page);
  servicesModule.factory('breadcrumbTrails', breadcrumbTrails);
  servicesModule.factory('routeUtil', routeUtil);
  servicesModule.factory('camAPI', camAPI);

  module.exports = servicesModule;
