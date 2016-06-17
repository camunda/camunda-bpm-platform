  'use strict';

  var Resource = [ '$resource', 'Uri', function($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/job/:id/:action'), { id: '@id' }, {
      query: { method: 'POST', isArray: true},
      count: { method: 'POST', isArray: false, params: { id: 'count' }},
      setRetries: { method: 'PUT', params: {'action': 'retries'} }
    });
  }];

  module.exports = Resource;
