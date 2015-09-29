define([
  'angular',

  './transform',
  './variables',
  './page',
  './breadcrumbTrails',
  './routeUtil',
  './cam-api',
  './cam-api-http-client'
], function(
  angular,

   transform,
   variables,
   page,
   breadcrumbTrails,
   routeUtil,
   camAPI,
   camAPIHttpClient
) {

  'use strict';

  var servicesModule = angular.module('cam.cockpit.services', []);

  servicesModule.factory('Transform', transform);
  servicesModule.factory('Variables', variables);
  servicesModule.service('page', page);
  servicesModule.factory('breadcrumbTrails', breadcrumbTrails);
  servicesModule.factory('routeUtil', routeUtil);
  servicesModule.factory('camAPIHttpClient', camAPIHttpClient);
  servicesModule.factory('camAPI', camAPI);

  return servicesModule;

});
