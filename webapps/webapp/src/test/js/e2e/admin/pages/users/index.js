'use strict'

var UsersPage = require('./users');
var EditUserProfilePage = require('./editUserProfile');
var EditUserAccountPage = require('./editUserAccount');
var EditUserGroupsPage = require('./editUserGroups');
var EditUserSelectGroupsPage = require('./editUserGroupsModalSelect');
var CreateNewUserPage = require('./createNewUser');
var AdminUserSetupPage = require('./adminSetup');
var AdminUserSetupStatusPage = require('./adminSetupStatus');

module.exports = new UsersPage();
module.exports.editUserProfile = new EditUserProfilePage();
module.exports.editUserAccount = new EditUserAccountPage();
module.exports.editUserGroups = new EditUserGroupsPage();
module.exports.editUserGroups.selectGroup = new EditUserSelectGroupsPage();
module.exports.createNewUser = new CreateNewUserPage();
module.exports.adminUserSetup = new AdminUserSetupPage();
module.exports.adminUserSetup.Status = new AdminUserSetupStatusPage();
