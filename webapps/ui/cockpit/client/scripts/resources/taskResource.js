  'use strict';
  var TaskResource = [ '$resource', 'Uri', function($resource, Uri) {
    var endpoint = Uri.appUri('engine://engine/:engine/task/:id/:action/:subAction');
    var endpointParams = { id: '@id' };

    return $resource(endpoint, endpointParams, {
      query: {
        method: 'POST',
        isArray: true
      },
      count: {
        method: 'POST',
        isArray: false,
        params: { id: 'count' }
      },

      getIdentityLinks: {
        method: 'GET',
        isArray: true,
        params: { action: 'identity-links' }
      },
      addIdentityLink: {
        method: 'POST',
        params: { action: 'identity-links' }
      },
      deleteIdentityLink: {
        method: 'POST',
        params: {
          action: 'identity-links',
          subAction: 'delete'
        }
      },

      setAssignee: {
        method: 'POST',
        params: { action: 'assignee' }
      }
    });
  }];

  module.exports = TaskResource;
