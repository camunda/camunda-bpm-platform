'use strict';

var RepositoryPage = require('./repository-view');
var DeploymentsPage = require('./deployments');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new RepositoryPage();
module.exports.deployments = new DeploymentsPage();

module.exports.authentication = new AuthenticationPage();