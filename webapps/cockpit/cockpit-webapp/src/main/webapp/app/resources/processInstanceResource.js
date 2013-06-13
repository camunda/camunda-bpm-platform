'use strict';

ngDefine('cockpit.resources', function(module) {
  
  var Resource = function ($resource, Uri) {
    return $resource(Uri.appUri('engine://process-instance/:id/:action'), { id: '@id' }, {
      count: { method: 'GET', isArray: true, params: { id: 'count' }},
      activityInstances: { method: 'GET', isArray: false, params: { action: 'activity-instances' }}
    });
  };

  module
    .factory('ProcessInstanceResource', Resource);

});
