'use strict';

var AuthorizationsPage = require('./authorizations-base');
var ApplicationPage = require('./application');
var AuthorizationPage = require('./authorization');
var DeploymentPage = require('./deployment');
var FilterPage = require('./filter');
var GroupPage = require('./group');
var GroupMembershipPage = require('./group-membership');
var ProcessDefinitionPage = require('./process-definition');
var DecisionDefinitionPage = require('./decision-definition');
var ProcessInstancePage = require('./process-instance');
var TaskPage = require('./task');
var UserPage = require('./user');
var BatchPage = require('./batch');
var TenantPage = require('./tenant');
var TenantMembershipPage = require('./tenant-membership');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

module.exports = new AuthorizationsPage();
module.exports.application = new ApplicationPage();
module.exports.authorization = new AuthorizationPage();
module.exports.deployment = new DeploymentPage();
module.exports.filter = new FilterPage();
module.exports.group = new GroupPage();
module.exports.groupMembership = new GroupMembershipPage();
module.exports.processDefinition = new ProcessDefinitionPage();
module.exports.decisionDefinition = new DecisionDefinitionPage();
module.exports.processInstance = new ProcessInstancePage();
module.exports.task = new TaskPage();
module.exports.user= new UserPage();
module.exports.batch = new BatchPage();
module.exports.tenant = new TenantPage();
module.exports.tenantMembership = new TenantMembershipPage();
module.exports.authentication = new AuthenticationPage();

