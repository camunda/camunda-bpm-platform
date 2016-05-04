'use strict';

var UsersPage = require('./users-dashboard');
var EditUserProfilePage = require('./edit-profile');
var EditUserAccountPage = require('./edit-account');
var EditUserGroupsPage = require('./edit-groups');
var EditUserGroupsModalPage = require('./edit-groups-modal');
var EditUserTenantsPage = require('./edit-tenants');
var EditUserTenantsModalPage = require('./edit-tenants-modal');
var NewUserPage = require('./new');
var AdminUserSetupPage = require('./admin-setup');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

module.exports = new UsersPage();
module.exports.editUserProfile = new EditUserProfilePage();
module.exports.editUserAccount = new EditUserAccountPage();
module.exports.editUserGroups = new EditUserGroupsPage();
module.exports.editUserGroups.selectGroupModal = new EditUserGroupsModalPage();
module.exports.editUserTenants = new EditUserTenantsPage();
module.exports.editUserTenants.selectTenantModal = new EditUserTenantsModalPage();
module.exports.newUser = new NewUserPage();
module.exports.adminUserSetup = new AdminUserSetupPage();
module.exports.authentication = new AuthenticationPage();
