'use strict';

ngDefine('cockpit.resources', function(module) {
  
  var Resource = function ($resource, Uri) {
    return $resource(Uri.appUri('engine://process-instance/:id'), { id: '@id' }, {
      count: { method: 'GET', isArray: true, params: { id: 'count' }}
    });
  };

  module
    .factory('ProcessInstanceResource', Resource);

});
