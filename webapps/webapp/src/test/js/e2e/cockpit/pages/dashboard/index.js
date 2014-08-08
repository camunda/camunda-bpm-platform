'use strict'

var DashboardPage = require('./dashboard-view');
var DeployedProcessesListPage = require('./deployed-processes-list');
var DeployedProcessesIconPage = require('./deployed-processes-icon');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new DashboardPage();
module.exports.deployedProcessesList = new DeployedProcessesListPage();
module.exports.deployedProcessesIcon = new DeployedProcessesIconPage();
module.exports.authentication = new AuthenticationPage();
