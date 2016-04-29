'use strict';

var TenantsPage = require('./tenants-dashboard');
var EditTenantPage = require('./edit');
var NewTenantPage = require('./new');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

module.exports = new TenantsPage();
module.exports.editTenant = new EditTenantPage();
module.exports.newTenant = new NewTenantPage();
module.exports.authentication = new AuthenticationPage();
