'use strict';

var ProcessDefinitionPage = require('./definition-view');
var DiagramPage = require('./../diagram');
var TableProcessInstancesPage = require('./tabs/process-instances-tab');
var TableCalledProcessDefinitionsPage = require('./tabs/called-process-definitions-tab');
var TableJobDefinitionsPage = require('./tabs/job-definitions-tab');
var SuspensionPage = require('./action-bar/suspension');
var SuspensionModalPage = require('./action-bar/suspension-modal');
var FilterPage = require('./filter');
var InformationPage = require('./../sidebar-information');

module.exports = new ProcessDefinitionPage();
module.exports.diagram = new DiagramPage();
module.exports.processInstancesTab = new TableProcessInstancesPage();
module.exports.calledProcessDefinitionsTab = new TableCalledProcessDefinitionsPage();
module.exports.jobDefinitionsTab = new TableJobDefinitionsPage();
module.exports.jobDefinitionsTab.modal = new SuspensionModalPage();
module.exports.suspension = new SuspensionPage();
module.exports.suspension.modal = new SuspensionModalPage();
module.exports.filter = new FilterPage();
module.exports.information = new InformationPage();
