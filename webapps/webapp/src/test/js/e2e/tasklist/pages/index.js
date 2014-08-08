'use strict';

var TasklistDashboardPage = require('./dashboard-view');
var AuthenticationPage = require('./../../commons/pages/authentication');

module.exports = new TasklistDashboardPage();
module.exports.authentication = new AuthenticationPage();

