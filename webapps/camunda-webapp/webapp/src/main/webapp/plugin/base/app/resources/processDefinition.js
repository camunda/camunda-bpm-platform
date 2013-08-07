ngDefine('cockpit.plugin.base.resources', function(module) {

  var Resource = function ($resource, Uri) {
    return $resource(Uri.appUri('plugin://base/:engine/process-definition/:id/:action'), { id: '@id' }, {
      getCalledProcessDefinitions: { method: 'POST', isArray: true, params: {action: 'called-process-definitions'}}
    });
  };

  module
    .factory('PluginProcessDefinitionResource', Resource);

});