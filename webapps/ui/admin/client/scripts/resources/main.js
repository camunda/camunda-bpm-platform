'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    userResource = require('./userResource'),
    groupResource = require('./groupResource'),
    groupMembershipResource = require('./groupMembershipResource'),
    initialUserResource = require('./initialUserResource'),
    metricsResource = require('./metricsResource'),
    tenantResource = require('./tenantResource');

var ngModule = angular.module('admin.resources', []);

ngModule.factory('UserResource', userResource);
ngModule.factory('GroupResource', groupResource);
ngModule.factory('GroupMembershipResource', groupMembershipResource);
ngModule.factory('InitialUserResource', initialUserResource);
ngModule.factory('MetricsResource', metricsResource);
ngModule.factory('TenantResource', tenantResource);

module.exports = ngModule;
