/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
