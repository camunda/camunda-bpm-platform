'use strict';

ngDefine('cockpit.resources.incident', ['module:camunda.common.services:camunda-common/services/uri'], function(module) {
  
  var Resource = function ($resource, Uri) {
    return $resource(Uri.appUri('plugin://base/default/process-instance/:id/incidents'), { id: '@id' }, {});
  };

  module
    .factory('IncidentResource', Resource);

});
