/* global ngDefine: false */
ngDefine('cockpit.plugin.base.resources', function(module) {
  'use strict';

  module.factory('PluginProcessInstanceResource', [
    '$resource',
    'Uri',
  function ($resource, Uri) {

    return $resource(Uri.appUri('plugin://base/:engine/process-instance/:id/:action'), {id: '@id'}, {
      query: {
        method: 'POST',
        isArray: true
      },

      count: {
        method: 'POST',
        isArray: false,
        params: { id: 'count' }
      },

      processInstances: {
        method: 'POST',
        isArray: true,
        params: {
          action: 'called-process-instances'
        }
      },

      // deprecated
      getCalledProcessInstances: {
        method: 'POST',
        isArray: true,
        params: {
          action: 'called-process-instances'
        }
      }
    });
  }]);
});
