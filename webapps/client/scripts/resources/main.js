define(['angular',
	'./userResource',
	'./groupResource',
	'./groupMembershipResource',
	'./initialUserResource',
  './metricsResource'
], function(angular,
	userResource,
	groupResource,
	groupMembershipResource,
	initialUserResource,
  metricsResource) {
  'use strict';

  var ngModule = angular.module('admin.resources', []);

  ngModule.factory('UserResource', userResource);
  ngModule.factory('GroupResource', groupResource);
  ngModule.factory('GroupMembershipResource', groupMembershipResource);
  ngModule.factory('InitialUserResource', initialUserResource);
  ngModule.factory('MetricsResource', metricsResource);

  return ngModule;
});
