'use strict';

var DashboardPage = require('./dashboard-view');
var StartProcessPage = require('./start-process');
var TasksPage = require('./tasks');
var TaskPage = require('./task');
var FilterPage = require('./filter');
var InvoiceStartFormPage = require('./invoice-start-form');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new DashboardPage();
module.exports.filter = new FilterPage();
module.exports.tasks = new TasksPage();
module.exports.task = new TaskPage();
module.exports.task.invoiceStartForm = new InvoiceStartFormPage();
module.exports.startProcess = new StartProcessPage();
module.exports.startProcess.invoiceStartForm = new InvoiceStartFormPage();
module.exports.authentication = new AuthenticationPage();
