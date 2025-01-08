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

var angular = require('camunda-commons-ui/vendor/angular');

require('angular-route');
require('camunda-commons-ui');

var authorizations = require('./authorizations'),
  authorizationCreate = require('./authorizationCreate'),
  authorizationDeleteConfirm = require('./authorizationDeleteConfirm'),
  users = require('./users'),
  dashboard = require('./dashboard'),
  userCreate = require('./userCreate'),
  userEdit = require('./userEdit'),
  groups = require('./groups'),
  groupCreate = require('./groupCreate'),
  groupEdit = require('./groupEdit'),
  groupMembershipsCreate = require('./groupMembershipsCreate'),
  setup = require('./setup'),
  system = require('./system'),
  systemSettingsGeneral = require('./systemSettingsGeneral'),
  tenants = require('./tenants'),
  tenantCreate = require('./tenantCreate'),
  tenantEdit = require('./tenantEdit'),
  tenantMembershipCreate = require('./tenantMembershipsCreate'),
  executionMetrics = require('./execution-metrics'),
  diagnostics = require('./diagnostics');

var ngModule = angular.module('cam.admin.pages', ['ngRoute', 'cam.commons']);

ngModule.config(authorizations);
ngModule.controller('AuthorizationCreateController', authorizationCreate);
ngModule.controller(
  'ConfirmDeleteAuthorizationController',
  authorizationDeleteConfirm
);
ngModule.config(dashboard);
ngModule.config(users);
ngModule.config(userCreate);
ngModule.config(userEdit);
ngModule.config(groups);
ngModule.config(groupCreate);
ngModule.config(groupEdit);
ngModule.controller('GroupMembershipDialogController', groupMembershipsCreate);
ngModule.config(setup);
ngModule.config(system);
ngModule.config(systemSettingsGeneral);
ngModule.config(tenants);
ngModule.config(tenantCreate);
ngModule.config(tenantEdit);
ngModule.controller('TenantMembershipDialogController', tenantMembershipCreate);
ngModule.config(executionMetrics);
ngModule.config(diagnostics);

module.exports = ngModule;
