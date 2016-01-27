'use strict'

var DashboardPage = require('./dashboard-view');
var DeployedProcessesListPage = require('./deployed-processes-list');
var DeployedProcessesPreviewsPage = require('./deployed-processes-previews');
var DeployedDecisionsListPage = require('./deployed-decisions-list');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new DashboardPage();

module.exports.deployedProcessesList = new DeployedProcessesListPage();
module.exports.deployedProcessesPreviews = new DeployedProcessesPreviewsPage();

module.exports.deployedDecisionsList = new DeployedDecisionsListPage();

module.exports.authentication = new AuthenticationPage();
