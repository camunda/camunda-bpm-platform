ngDefine('admin.resources', function(module) {

  var GroupMembershipResource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/group/:groupId/members/:userId'), { groupId: '@groupId' , userId: '@userId'}, {
      create : {method:'PUT'}
    });
  }];

  module.factory('GroupMembershipResource', GroupMembershipResource);
});