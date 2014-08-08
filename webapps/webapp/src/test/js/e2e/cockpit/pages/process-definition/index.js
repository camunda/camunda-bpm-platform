'use strict';

var ProcessDefinitionPage = require('./definition-view');
var DiagramPage = require('./../diagram');
var TablePage = require('./../table');
var TableProcessInstancesPage = require('./table-process-instances');
var TableCalledProcessDefinitionsPage = require('./table-called-process-definitions');

module.exports = new ProcessDefinitionPage();
module.exports.diagram = new DiagramPage();
module.exports.table = new TablePage();
module.exports.table.processInstancesTab = new TableProcessInstancesPage();
module.exports.table.calledProcessDefinitionsTab = new TableCalledProcessDefinitionsPage();