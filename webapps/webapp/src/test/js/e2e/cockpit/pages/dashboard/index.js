'use strict'

var DasboardPage = require('./dashboard-view');
var DeployedProcessesListPage = require('./deployed-processes-list');
var DeployedProcessesIconPage = require('./deployed-processes-icon');

module.exports = new DasboardPage();
module.exports.deployedProcessesList = new DeployedProcessesListPage();
module.exports.deployedProcessesIcon = new DeployedProcessesIconPage();
