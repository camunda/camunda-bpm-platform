'use strict';

var angular = require('angular');

var externalTasks = require('./services/external-tasks');

var externalTaskActivityLink = require('./components/external-task-activity-link');

var ExternalTaskActivityLinkController = require('./controllers/external-task-activity-link-controller');

var ngModule = angular.module('cam-common.external-tasks.common', []);

ngModule.factory('externalTasks', externalTasks);

ngModule.directive('externalTaskActivityLink', externalTaskActivityLink);

ngModule.controller('ExternalTaskActivityLinkController', ExternalTaskActivityLinkController);

module.exports = ngModule;
