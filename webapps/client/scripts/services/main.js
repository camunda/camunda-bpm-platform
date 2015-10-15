define([
  'angular',

  './transform',
  './variables',
  './page',
  './breadcrumbTrails',
  './routeUtil',
  './cam-api'
], function(
  angular,

   transform,
   variables,
   page,
   breadcrumbTrails,
   routeUtil,
   camAPI
) {

  'use strict';

  var servicesModule = angular.module('cam.cockpit.services', []);

  servicesModule.factory('Transform', transform);
  servicesModule.factory('Variables', variables);
  servicesModule.service('page', page);
  servicesModule.factory('breadcrumbTrails', breadcrumbTrails);
  servicesModule.factory('routeUtil', routeUtil);
  servicesModule.factory('camAPI', camAPI);

  return servicesModule;

});
