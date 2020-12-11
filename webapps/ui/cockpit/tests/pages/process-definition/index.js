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

var ProcessDefinitionPage = require('./definition-view');
var DiagramPage = require('./../diagram');
var TableProcessInstancesPage = require('./tabs/process-instances-tab');
var TableCalledProcessDefinitionsPage = require('./tabs/called-process-definitions-tab');
var TableJobDefinitionsPage = require('./tabs/job-definitions-tab');
var SuspensionPage = require('./action-bar/suspension');
var SuspensionModalPage = require('./action-bar/suspension-modal');
var InformationPage = require('./../sidebar-information');
var SearchWidget = require('../../../../common/tests/pages/search-widget');

module.exports = new ProcessDefinitionPage();
module.exports.diagram = new DiagramPage();
module.exports.processInstancesTab = new TableProcessInstancesPage();
module.exports.calledProcessDefinitionsTab = new TableCalledProcessDefinitionsPage();
module.exports.jobDefinitionsTab = new TableJobDefinitionsPage();
module.exports.jobDefinitionsTab.modal = new SuspensionModalPage();
module.exports.suspension = new SuspensionPage();
module.exports.suspension.modal = new SuspensionModalPage();
module.exports.information = new InformationPage();
module.exports.search = new SearchWidget();
