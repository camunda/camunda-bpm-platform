'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

// Services
var observeBpmnElements = require('./services/observe-bpmn-elements');

// Components
var externalTaskActivityLink = require('./components/external-task-activity-link');
var externalTasksTab = require('./components/external-tasks-tab');

// Controllers
var ExternalTaskActivityLinkController = require('./controllers/external-task-activity-link-controller');
var ExternalTasksTabController = require('./controllers/external-tasks-tab-controller');

var ngModule = angular.module('cam-common.external-tasks-common', []);

// Services
ngModule.factory('observeBpmnElements', observeBpmnElements);

// Components
ngModule.directive('externalTaskActivityLink', externalTaskActivityLink);
ngModule.directive('externalTasksTab', externalTasksTab);

// Controllers
ngModule.controller('ExternalTaskActivityLinkController', ExternalTaskActivityLinkController);
ngModule.controller('ExternalTasksTabController', ExternalTasksTabController);

module.exports = ngModule;
