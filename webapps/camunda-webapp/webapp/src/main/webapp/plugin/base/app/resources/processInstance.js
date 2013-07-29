ngDefine('cockpit.plugin.base.resources', function(module) {

  var Resource = function ($resource, Uri) {
    return $resource(Uri.appUri('plugin://base/:engine/process-instance/:id/:action'), {id: '@id'}, {
      query: { method: 'POST', isArray: true},
      count: { method: 'POST', isArray: false, params: { id: 'count' }},
      getCalledProcessInstances: {method: 'POST', isArray: true, params: {action: 'called-process-instances'}}
    });
  };

  module
    .factory('PluginProcessInstanceResource', Resource);

});