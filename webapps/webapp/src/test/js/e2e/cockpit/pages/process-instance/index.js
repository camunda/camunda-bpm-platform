'use strict';

var ProcessInstancePage = require('./instance-view');
var DiagramPage = require('./../diagram');
var TableVariablesPage = require('./tabs/variables-tab');
var TableIncidentsPage = require('./tabs/incidents-tab');
var TableCalledProcessInstancesPage = require('./tabs/called-process-instances-tab');
var TableUserTaskPage = require('./tabs/user-tasks-tab');
var ActionBarPage = require('./instance-runtime-action');
var InstanceTreePage = require('./instance-tree');

module.exports = new ProcessInstancePage();
module.exports.diagram = new DiagramPage();
module.exports.variablesTab = new TableVariablesPage();
module.exports.incidentTab = new TableIncidentsPage();
module.exports.calledProcessInstancesTab = new TableCalledProcessInstancesPage();
module.exports.userTaskTab = new TableUserTaskPage();
module.exports.actionBar = new ActionBarPage();
module.exports.instanceTree = new InstanceTreePage();
