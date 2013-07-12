ngDefine('admin.resources', function(module) {

  var UserResource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://user/:userId/:action'), { userId: '@id' }, {
      profile : {method:'GET', params: { 'action' : 'profile'}},
      updateProfile : {method:'PUT', params: { 'action' : 'profile'}}
    });
  }];

  module.factory('UserResource', UserResource);
});