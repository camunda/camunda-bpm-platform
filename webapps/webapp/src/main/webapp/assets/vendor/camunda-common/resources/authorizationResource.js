ngDefine('camunda.common.resources.authorization', [
  'module:camunda.common.services.uri:../services/uri'
], function(module) {

  var AuthorizationResource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/authorization/:action'), { action: '@action' }, {
      check : {method:'GET', params: { 'action' : 'check'},  cache : true},
      create : {method:'POST', params: { 'action' : 'create'}}
    });  

  }];

  module.factory('AuthorizationResource', AuthorizationResource);
});