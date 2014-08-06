'use strict';

var ProcessDefinitionPage = require('./definition-view');
var DiagramPage = require('./../diagram');
var TablePage = require('./../table');
var TableProcessInstancePage = require('./table-process-instances');

module.exports = new ProcessDefinitionPage();
module.exports.diagram = new DiagramPage();
module.exports.table = new TablePage();
module.exports.table.processInstanceTab = new TableProcessInstancePage();