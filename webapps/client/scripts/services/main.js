define([
  'angular',

  './transform',
  './variables',
  './page',
  './breadcrumbTrails',
  './routeUtil'
], function(
  angular,

   transform,
   variables,
   page,
   breadcrumbTrails,
   routeUtil
) {

  'use strict';

  var servicesModule = angular.module('cam.cockpit.services', []);

  servicesModule.factory('Transform', transform);
  servicesModule.factory('Variables', variables);
  servicesModule.service('page', page);
  servicesModule.factory('breadcrumbTrails', breadcrumbTrails);
  servicesModule.factory('routeUtil', routeUtil);

  return servicesModule;

});
