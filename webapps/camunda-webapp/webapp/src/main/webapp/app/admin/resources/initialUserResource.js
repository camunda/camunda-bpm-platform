ngDefine('admin.resources', function(module) {

  var InitialUserResource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('admin://setup/:engine/user/:action'), { action: '@action' }, {
      create : {method:'POST', params: { 'action' : 'create'}}
    });
  }];

  module.factory('InitialUserResource', InitialUserResource);
});