'use strict';

var DashboardPage = require('./dashboard-view');
var StartProcessPage = require('./start-process');
var TaskListPage = require('./tasklist/task-list');
var CurrentTaskPage = require('./taskview/current-task');
var TaskFiltersPage = require('./filter/task-filters');
var CreateFilterPage = require('./filter/create-filter');
var EditFilterPage = require('./filter/edit-filter');
var DeleteFilterPage = require('./filter/delete-filter');
var InvoiceStartFormPage = require('./forms/invoice-start-form');
var GenericStartFormPage = require('./forms/generic-start-form');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new DashboardPage();
module.exports.taskFilters = new TaskFiltersPage();
module.exports.taskFilters.createFilterPage = new CreateFilterPage();
module.exports.taskFilters.editFilterPage = new EditFilterPage();
module.exports.taskFilters.deleteFilterPage = new DeleteFilterPage();
module.exports.taskList = new TaskListPage();
module.exports.currentTask = new CurrentTaskPage();
module.exports.currentTask.invoiceStartForm = new InvoiceStartFormPage();
module.exports.startProcess = new StartProcessPage();
module.exports.startProcess.invoiceStartForm = new InvoiceStartFormPage();
module.exports.startProcess.genericStartForm = new GenericStartFormPage();
module.exports.authentication = new AuthenticationPage();
