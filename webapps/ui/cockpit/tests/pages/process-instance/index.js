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

var ProcessInstancePage = require('./instance-view');
var DiagramPage = require('./../diagram');
var TableVariablesPage = require('./tabs/variables-tab');
var TableIncidentsPage = require('./tabs/incidents-tab');
var TableCalledInstancesPage = require('./tabs/called-process-instances-tab');
var TableUserTasksPage = require('./tabs/user-tasks-tab');
var AddVariablePage = require('./action-bar/add-variable');
var SuspensionPage = require('./action-bar/suspension');
var CancelInstancePage = require('./action-bar/cancel-instance');
var CancelInstanceModalPage = require('./action-bar/cancel-instance-modal');
var RetryFailedJobPage = require('./action-bar/retry-failed-job');
var SuspensionModalPage = require('./action-bar/suspension-modal');
var InstanceTreePage = require('./instance-tree');
var IdentityLinksModalPage = require('./modals/identity-links-modal');
var InformationPage = require('./../sidebar-information');
var SearchWidget = require('../../../../common/tests/pages/search-widget');

module.exports = new ProcessInstancePage();
module.exports.diagram = new DiagramPage();
module.exports.variablesTab = new TableVariablesPage();
module.exports.incidentsTab = new TableIncidentsPage();
module.exports.calledInstancesTab = new TableCalledInstancesPage();
module.exports.userTasksTab = new TableUserTasksPage();
module.exports.userTasksTab.modal = new IdentityLinksModalPage();
module.exports.addVariable = new AddVariablePage();
module.exports.suspension = new SuspensionPage();
module.exports.suspension.modal = new SuspensionModalPage();
module.exports.cancelInstance = new CancelInstancePage();
module.exports.cancelInstance.modal = new CancelInstanceModalPage();
module.exports.retryFailedJob = new RetryFailedJobPage();
module.exports.instanceTree = new InstanceTreePage();
module.exports.information = new InformationPage();
module.exports.search = new SearchWidget();
