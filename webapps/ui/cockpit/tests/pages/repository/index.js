'use strict';

var RepositoryPage = require('./repository-view');
var DeploymentsPage = require('./deployments');
var ResourcesPage = require('./resources');
var ResourcePage = require('./resource');
var DefinitionTabPage = require('./tabs/definitions-tab');
var DrdTablePage = require('./tabs/drd-table');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

module.exports = new RepositoryPage();
module.exports.deployments = new DeploymentsPage();
module.exports.resources = new ResourcesPage();
module.exports.resource = new ResourcePage();
module.exports.resource.definitions = new DefinitionTabPage();
module.exports.resource.drdTable = new DrdTablePage();

module.exports.authentication = new AuthenticationPage();
