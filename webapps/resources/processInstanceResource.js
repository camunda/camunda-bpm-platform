/* global ngDefine: false */
ngDefine('cockpit.resources', function(module) {
  'use strict';

  module.factory('ProcessInstanceResource', [
    '$resource',
    'Uri',
  function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/process-instance/:id/:action'), { id: '@id' }, {
      query: {
        method: 'POST',
        isArray: true
      },

      count: {
        method: 'POST',
        isArray: false,
        params: { id: 'count' }
      },

      activityInstances: {
        method: 'GET',
        isArray: false,
        params: {
          action: 'activity-instances'
        }
      }
    });
  }]);
});
