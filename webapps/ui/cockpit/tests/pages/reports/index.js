'use strict';

var ReportsView = require('./reports-view');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

module.exports = new ReportsView();

module.exports.authentication = new AuthenticationPage();
