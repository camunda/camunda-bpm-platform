/* global define: false */

/**
 * @namespace cam.cockpit.plugin.base.views
 */
define([
  'angular',

  // dashboard
  './dashboard/decision-list',

  // decision definition
  './decisionDefinition/decisionInstanceTable'

], function(angular, decisionList, decisionInstanceTable) {

  'use strict';
  var ngModule = angular.module('cockpit.plugin.decisionList.views', []);

  ngModule.config(decisionList);
  ngModule.config(decisionInstanceTable);

  return ngModule;
});
