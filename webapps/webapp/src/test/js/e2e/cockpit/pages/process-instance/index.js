'use strict';

var ProcessInstancePage = require('./instance-view');
var DiagramPage = require('./../diagram');
var TablePage = require('./../table');
var TableVariablesPage = require('./tabs/variables-tab');
var TableIncidentsPage = require('./tabs/incidents-tab');
var TableCalledProcessInstancesPage = require('./tabs/called-process-instances-tab');
var TableUserTaskPage = require('./tabs/user-tasks-tab');
var ActionBarPage = require('./instance-runtime-action');
var InstanceTreePage = require('./instance-tree');

module.exports = new ProcessInstancePage();
module.exports.diagram = new DiagramPage();
module.exports.table = new TablePage();
module.exports.table.variablesTab = new TableVariablesPage();
module.exports.table.incidentTab = new TableIncidentsPage();
module.exports.table.calledProcessInstancesTab = new TableCalledProcessInstancesPage();
module.exports.table.userTaskTab = new TableUserTaskPage();
module.exports.actionBar = new ActionBarPage();
module.exports.instanceTree = new InstanceTreePage();
