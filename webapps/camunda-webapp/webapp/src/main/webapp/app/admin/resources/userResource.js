ngDefine('admin.resources', function(module) {

  var UserResource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/user/:userId/:action'), { userId: '@id' }, {
      profile : {method:'GET', params: { 'action' : 'profile'}},
      updateProfile : {method:'PUT', params: { 'action' : 'profile'}},
      updateCredentials : {method:'PUT', params: { 'action' : 'credentials'}},
      createUser : {method:'POST', params: { 'userId' : 'create'}},
      OPTIONS : {method:'OPTIONS', params: {}}
    });
  }];

  module.factory('UserResource', UserResource);
});