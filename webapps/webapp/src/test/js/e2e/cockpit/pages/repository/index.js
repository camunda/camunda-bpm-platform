'use strict';

var RepositoryPage = require('./repository-view');
var DeploymentsPage = require('./deployments');
var ResourcesPage = require('./resources');
var ResourcePage = require('./resource');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new RepositoryPage();
module.exports.deployments = new DeploymentsPage();
module.exports.resources = new ResourcesPage();
module.exports.resource = new ResourcePage();

module.exports.authentication = new AuthenticationPage();