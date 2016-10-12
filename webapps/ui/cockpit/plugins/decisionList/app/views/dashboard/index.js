'use strict';

var angular = require('angular');
var camCommon = require('cam-common');
var decisionList = require('./views/decision-list');
var DecisionListController = require('./controllers/decision-list');
var decisionListService = require('./services/decision-list');
var decisionsTableComponent = require('./components/decisions-table');
var drdsTableComponent = require('./components/drds-table');

var ngModule = angular.module('cockpit.plugin.decisionList.views.dashboard', [camCommon.name]);

ngModule.config(decisionList);

ngModule.factory('decisionList', decisionListService);

ngModule.controller('DecisionListController', DecisionListController);

ngModule.directive('decisionsTable', decisionsTableComponent);
ngModule.directive('drdsTable', drdsTableComponent);

module.exports = ngModule;
