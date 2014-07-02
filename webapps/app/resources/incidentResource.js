/* global ngDefine: false */
ngDefine('cockpit.resources', function(module) {
  'use strict';

  var Resource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/incident/:action'), { }, {
      count: { method: 'GET', isArray: false, params: { count: 'count' }}
    });
  }];

  module.factory('IncidentResource', Resource);
});
