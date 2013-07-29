ngDefine('admin.resources', function(module) {

  var GroupResource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/group/:groupId/:action'), { groupId: '@id' }, {
      createGroup : {method:'POST', params: { 'groupId' : 'create'}},
      update : {method:'PUT'}
    });
  }];

  module.factory('GroupResource', GroupResource);
});