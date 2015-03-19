'use strict';

var ProcessDefinitionPage = require('./definition-view');
var DiagramPage = require('./../diagram');
var TableProcessInstancesPage = require('./tabs/process-instances-tab');
var TableCalledProcessDefinitionsPage = require('./tabs/called-process-definitions-tab');
var TableJobDefinitionsPage = require('./tabs/job-definitions-tab');
var ActionBarPage = require('./definition-runtime-action');
var FilterPage = require('./filter');

module.exports = new ProcessDefinitionPage();
module.exports.diagram = new DiagramPage();
module.exports.processInstancesTab = new TableProcessInstancesPage();
module.exports.calledProcessDefinitionsTab = new TableCalledProcessDefinitionsPage();
module.exports.jobDefinitionsTab = new TableJobDefinitionsPage();
module.exports.actionBar = new ActionBarPage();
module.exports.filter = new FilterPage();
