define(['angular',
	'./userResource',
	'./groupResource',
	'./groupMembershipResource',
	'./initialUserResource'
], function(angular,
	userResource,
	groupResource,
	groupMembershipResource,
	initialUserResource) {

  var ngModule = angular.module('admin.resources', []);

  ngModule.factory('UserResource', userResource);
  ngModule.factory('GroupResource', groupResource);
  ngModule.factory('GroupMembershipResource', groupMembershipResource);
  ngModule.factory('InitialUserResource', initialUserResource);

  return ngModule;
});
