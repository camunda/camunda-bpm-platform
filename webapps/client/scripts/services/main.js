define([
  'angular',

  './transform',
  './variables',
  './page',
  './breadcrumbTrails'
], function(
  angular,

   transform,
   variables,
   page,
   breadcrumbTrails
) {

  'use strict';

  var servicesModule = angular.module('cam.cockpit.services', []);

  servicesModule.factory('Transform', transform);
  servicesModule.factory('Variables', variables);
  servicesModule.service('page', page);
  servicesModule.factory('breadcrumbTrails', breadcrumbTrails);

  return servicesModule;

});
