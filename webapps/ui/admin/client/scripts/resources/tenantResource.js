'use strict';

module.exports = [ '$resource', 'Uri', function($resource, Uri) {

  return $resource(Uri.appUri('engine://engine/:engine/tenant/:tenantId/:action'), { tenantId: '@id' }, {
    createTenant : {method:'POST', params: { 'tenantId' : 'create'}},
    update : {method:'PUT'},
    OPTIONS : {method:'OPTIONS', params: {}},
    count : {method: 'GET', params: { 'tenantId' : 'count'}}
  });
}];
