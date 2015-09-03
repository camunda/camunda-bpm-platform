/* global define: false */

/**
 * @namespace cam.cockpit.plugin.base.views
 */
define([
  'angular',

  // dashboard
  './dashboard/decision-list',

], function(angular, decisionList) {

  'use strict';
  var ngModule = angular.module('cockpit.plugin.decisionList.views', []);

  ngModule.config(decisionList);

  return ngModule;
});
