'use strict';

var DashboardPage = require('./dashboard-view');
var StartProcessPage = require('./start-process');
var TasksPage = require('./list');
var TaskPage = require('./task');
var PilesPage = require('./piles');
var InvoiceStartFormPage = require('./invoice-start-form');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new DashboardPage();
module.exports.piles = new PilesPage();
module.exports.tasks = new TasksPage();
module.exports.task = new TaskPage();
module.exports.startProcess = new StartProcessPage();
module.exports.startProcess.invoiceStartForm = new InvoiceStartFormPage();
module.exports.authentication = new AuthenticationPage();
