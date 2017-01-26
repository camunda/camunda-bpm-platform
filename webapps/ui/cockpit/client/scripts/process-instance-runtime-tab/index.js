'use strict';

var angular = require('camunda-commons-ui/vendor/angular');
var camCommon = require('../../../../common/scripts/module');
var externalTasksCommon = require('../../../../common/scripts/module/external-tasks-common');

var processInstanceRuntimeExternalTasks = require('./components/process-instance-runtime-external-tasks');

var ProcessInstanceRuntimeExternalTasksController = require('./controllers/process-instance-runtime-external-tasks-controller');

var viewConfig = require('./view-provider.config');

var ngModule = angular.module('cockpit.plugin.external-tasks.process-instance-runtime-tab', [
  camCommon.name,
  externalTasksCommon.name
]);

ngModule.directive('processInstanceRuntimeExternalTasks', processInstanceRuntimeExternalTasks);

ngModule.controller('ProcessInstanceRuntimeExternalTasksController', ProcessInstanceRuntimeExternalTasksController);

ngModule.config(viewConfig);

module.exports = ngModule;
