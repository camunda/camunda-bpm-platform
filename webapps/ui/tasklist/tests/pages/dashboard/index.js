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

var DashboardPage = require('./dashboard-view');
var StartProcessPage = require('./start-process-modal');
var TaskListPage = require('./tasklist/task-list');
var TaskListSearchPage = require('./tasklist/task-search');
var TaskListSortingPage = require('./tasklist/task-sorting');
var CurrentTaskPage = require('./taskview/current-task');
var FormTabPage = require('./taskview/tabs/form-tab');
var HistoryTabPage = require('./taskview/tabs/history-tab');
var DiagramTabPage = require('./taskview/tabs/diagram-tab');
var DescriptionTabPage = require('./taskview/tabs/description-tab');
var TaskFiltersPage = require('./filter/task-filters');
var CreateFilterPage = require('./filter/create-filter');
var EditFilterPage = require('./filter/edit-filter');
var DeleteFilterPage = require('./filter/delete-filter');
var InvoiceStartFormPage = require('./forms/invoice-start-form');
var GenericStartFormPage = require('./forms/generic-start-form');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');
var CreateTaskPage = require('./create-task-modal');

module.exports = new DashboardPage();
module.exports.taskFilters = new TaskFiltersPage();
module.exports.taskFilters.createFilterPage = new CreateFilterPage();
module.exports.taskFilters.editFilterPage = new EditFilterPage();
module.exports.taskFilters.deleteFilterPage = new DeleteFilterPage();
module.exports.taskList = new TaskListPage();
module.exports.taskList.taskSearch = new TaskListSearchPage();
module.exports.taskList.taskSorting = new TaskListSortingPage();
module.exports.currentTask = new CurrentTaskPage();
module.exports.currentTask.form = new FormTabPage();
module.exports.currentTask.history = new HistoryTabPage();
module.exports.currentTask.diagram = new DiagramTabPage();
module.exports.currentTask.description = new DescriptionTabPage();
module.exports.currentTask.invoiceStartForm = new InvoiceStartFormPage();
module.exports.startProcess = new StartProcessPage();
module.exports.startProcess.invoiceStartForm = new InvoiceStartFormPage();
module.exports.startProcess.genericStartForm = new GenericStartFormPage();
module.exports.authentication = new AuthenticationPage();
module.exports.createTask = new CreateTaskPage();
