'use strict';

var ProcessDefinitionPage = require('./definition-view');
var DiagramPage = require('./../diagram');
var TablePage = require('./../table');
var TableProcessInstancesPage = require('./process-instances-tab');
var TableCalledProcessDefinitionsPage = require('./called-process-definitions-tab');
var TableJobDefinitionsPage = require('./job-definitions-tab');

module.exports = new ProcessDefinitionPage();
module.exports.diagram = new DiagramPage();
module.exports.table = new TablePage();
module.exports.table.processInstancesTab = new TableProcessInstancesPage();
module.exports.table.calledProcessDefinitionsTab = new TableCalledProcessDefinitionsPage();
module.exports.table.jobDefinitionsTab = new TableJobDefinitionsPage();