'use strict';

var ProcessDefinitionPage = require('./definition-view');
var DiagramPage = require('./../diagram');
var TableProcessInstancesPage = require('./tabs/process-instances-tab');
var TableCalledProcessDefinitionsPage = require('./tabs/called-process-definitions-tab');
var TableJobDefinitionsPage = require('./tabs/job-definitions-tab');
var SuspensionPage = require('./action-bar/suspension');
var SuspensionModalPage = require('./action-bar/suspension-modal');
var InformationPage = require('./../sidebar-information');
var SearchWidget = require('../../../../common/tests/pages/search-widget');

module.exports = new ProcessDefinitionPage();
module.exports.diagram = new DiagramPage();
module.exports.processInstancesTab = new TableProcessInstancesPage();
module.exports.calledProcessDefinitionsTab = new TableCalledProcessDefinitionsPage();
module.exports.jobDefinitionsTab = new TableJobDefinitionsPage();
module.exports.jobDefinitionsTab.modal = new SuspensionModalPage();
module.exports.suspension = new SuspensionPage();
module.exports.suspension.modal = new SuspensionModalPage();
module.exports.information = new InformationPage();
module.exports.search = new SearchWidget();
