'use strict';

var angular = require('camunda-commons-ui/vendor/angular');
var camCommon = require('cam-common');

var externalTasks = require('./services/external-tasks');

var ProcessInstanceRuntimeTabController = require('./controllers/process-instance-runtime-external-tasks-controller');

var viewConfig = require('./view-provider.config');

var ngModule = angular.module('cockpit.plugin.process-instance-runtime-tab', [
  camCommon.name
]);

ngModule.factory('externalTasks', externalTasks);

ngModule.controller('ProcessInstanceRuntimeTabController', ProcessInstanceRuntimeTabController);

ngModule.config(viewConfig);

module.exports = ngModule;
