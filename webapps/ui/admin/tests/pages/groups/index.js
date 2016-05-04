'use strict';

var GroupsPage = require('./groups-dashboard');
var EditGroupPage = require('./edit');
var EditGroupTenantsPage = require('./edit-tenants');
var EditGroupTenantsModalPage = require('./edit-tenants-modal');
var NewGroupPage = require('./new');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

module.exports = new GroupsPage();
module.exports.editGroup = new EditGroupPage();
module.exports.editGroupTenants = new EditGroupTenantsPage();
module.exports.editGroupTenants.selectTenantModal = new EditGroupTenantsModalPage();
module.exports.newGroup = new NewGroupPage();
module.exports.authentication = new AuthenticationPage();
