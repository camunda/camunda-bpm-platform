'use strict';

var ProcessInstancePage = require('./instance-view');
var DiagramPage = require('./../diagram');
var TableVariablesPage = require('./tabs/variables-tab');
var TableIncidentsPage = require('./tabs/incidents-tab');
var TableCalledInstancesPage = require('./tabs/called-process-instances-tab');
var TableUserTasksPage = require('./tabs/user-tasks-tab');
var ActionBarPage = require('./instance-runtime-action');
var AddVariablePage = require('./actions/add-variable');
var InstanceTreePage = require('./instance-tree');

module.exports = new ProcessInstancePage();
module.exports.diagram = new DiagramPage();
module.exports.variablesTab = new TableVariablesPage();
module.exports.incidentsTab = new TableIncidentsPage();
module.exports.calledInstancesTab = new TableCalledInstancesPage();
module.exports.userTasksTab = new TableUserTasksPage();
module.exports.actionBar = new ActionBarPage();
module.exports.addVariable = new AddVariablePage();
module.exports.instanceTree = new InstanceTreePage();
