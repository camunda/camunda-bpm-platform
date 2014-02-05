/* global ngDefine: false */
ngDefine('cockpit.resources', function(module) {
  'use strict';

  var Resource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('plugin://base/:engine/process-instance/:id/incidents'), { id: '@id' }, {});
  }];

  module.factory('IncidentResource', Resource);
});
