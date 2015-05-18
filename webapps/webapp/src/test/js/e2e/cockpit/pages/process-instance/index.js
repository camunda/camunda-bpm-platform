'use strict';

var ProcessInstancePage = require('./instance-view');
var DiagramPage = require('./../diagram');
var TableVariablesPage = require('./tabs/variables-tab');
var TableIncidentsPage = require('./tabs/incidents-tab');
var TableCalledInstancesPage = require('./tabs/called-process-instances-tab');
var TableUserTasksPage = require('./tabs/user-tasks-tab');
var AddVariablePage = require('./action-bar/add-variable');
var SuspensionPage = require('./action-bar/suspension');
var CancelInstancePage = require('./action-bar/cancel-instance');
var RetryFailedJobPage = require('./action-bar/retry-failed-job');
var SuspensionModalPage = require('./action-bar/suspension-modal');
var InstanceTreePage = require('./instance-tree');

module.exports = new ProcessInstancePage();
module.exports.diagram = new DiagramPage();
module.exports.variablesTab = new TableVariablesPage();
module.exports.incidentsTab = new TableIncidentsPage();
module.exports.calledInstancesTab = new TableCalledInstancesPage();
module.exports.userTasksTab = new TableUserTasksPage();
module.exports.addVariable = new AddVariablePage();
module.exports.suspension = new SuspensionPage();
module.exports.suspension.modal = new SuspensionModalPage();
module.exports.cancelInstance = new CancelInstancePage();
module.exports.retryFailedJob = new RetryFailedJobPage();
module.exports.instanceTree = new InstanceTreePage();
