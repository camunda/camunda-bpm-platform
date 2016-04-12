'use strict';

var DashboardPage =       require('./dashboard-view');
var AuthenticationPage =  require('../../../../common/tests/pages/authentication');

var Breadcrumb =       require('../breadcrumb');

module.exports = new DashboardPage();
module.exports.authentication = new AuthenticationPage();

module.exports.breadcrumb = new Breadcrumb();
