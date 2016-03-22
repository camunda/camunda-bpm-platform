'use strict';

var DashboardPage = require('./dashboard-view');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

module.exports = new DashboardPage();
module.exports.authentication = new AuthenticationPage();
