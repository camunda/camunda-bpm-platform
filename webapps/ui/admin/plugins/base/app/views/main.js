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

/**
 * @namespace cam.cockpit.plugin.base.views
 */
'use strict';

var angular = require('angular'),
  camCommon = require('cam-common'),
  // dashboard
  dashboardUsers = require('./dashboard/users'),
  dashboardGroups = require('./dashboard/groups'),
  dashboardTenants = require('./dashboard/tenants'),
  dashboardAuthorizations = require('./dashboard/authorizations'),
  dashboardSystem = require('./dashboard/system');

var ngModule = angular.module('cockpit.plugin.base.views', [camCommon.name]);

ngModule.config(dashboardUsers);
ngModule.config(dashboardGroups);
ngModule.config(dashboardTenants);
ngModule.config(dashboardAuthorizations);
ngModule.config(dashboardSystem);

module.exports = ngModule;
