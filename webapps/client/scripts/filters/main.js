define([
  'angular',

  './shorten',
  './abbreviateNumber',
  './duration'

], function(
  angular,

   shorten,
   abbreviateNumber,
   duration
) {

  'use strict';

  var filtersModule = angular.module('cam.cockpit.filters', []);

  filtersModule.filter('shorten', shorten);
  filtersModule.filter('abbreviateNumber', abbreviateNumber);
  filtersModule.filter('duration', duration);

  return filtersModule;
});
