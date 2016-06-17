'use strict';

module.exports = [ '$resource', 'Uri', function($resource, Uri) {

  return $resource(Uri.appUri('engine://engine/:engine/group/:groupId/:action'), { groupId: '@id' }, {
    createGroup : {method:'POST', params: { 'groupId' : 'create'}},
    update : {method:'PUT'},
    OPTIONS : {method:'OPTIONS', params: {}},
    count : {method: 'GET', params: { 'groupId' : 'count'}}
  });
}];
