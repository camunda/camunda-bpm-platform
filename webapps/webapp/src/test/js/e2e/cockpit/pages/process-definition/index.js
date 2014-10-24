'use strict';

var ProcessDefinitionPage = require('./definition-view');
var DiagramPage = require('./../diagram');
var TablePage = require('./../table');
var TableProcessInstancesPage = require('./tabs/process-instances-tab');
var TableCalledProcessDefinitionsPage = require('./tabs/called-process-definitions-tab');
var TableJobDefinitionsPage = require('./tabs/job-definitions-tab');
var ActionBarPage = require('./definition-runtime-action');

module.exports = new ProcessDefinitionPage();
module.exports.diagram = new DiagramPage();
module.exports.table = new TablePage();
module.exports.table.processInstancesTab = new TableProcessInstancesPage();
module.exports.table.calledProcessDefinitionsTab = new TableCalledProcessDefinitionsPage();
module.exports.table.jobDefinitionsTab = new TableJobDefinitionsPage();
module.exports.actionBar = new ActionBarPage();