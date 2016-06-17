  'use strict';

  module.exports = [
    '$resource',
    'Uri',
    function($resource, Uri) {

      return $resource(Uri.appUri('plugin://base/:engine/process-instance/:id/:action'), {id: '@id'}, {
        query: {
          method: 'POST',
          isArray: true
        },

        delete: {
          url: Uri.appUri('engine://engine/:engine/process-instance/:id'),
          method: 'DELETE'
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
    }];
