/**
 * @namespace cam.cockpit.plugin.base.views
 */
'use strict';

var angular = require('angular'),

    // dashboard
    dashboardUsers = require('./dashboard/users'),
    dashboardGroups = require('./dashboard/groups'),
    dashboardTenants = require('./dashboard/tenants'),
    dashboardAuthorizations = require('./dashboard/authorizations'),
    dashboardSystem = require('./dashboard/system');

var ngModule = angular.module('cockpit.plugin.base.views', []);

ngModule.config(dashboardUsers);
ngModule.config(dashboardGroups);
ngModule.config(dashboardTenants);
ngModule.config(dashboardAuthorizations);
ngModule.config(dashboardSystem);

module.exports = ngModule;
