ngDefine('cockpit.resources', function(module) {

  var Resource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/process-definition/:id/:action'), { id: '@id' }, {
      queryStatistics: { method: 'GET', isArray: true, params: { id: 'statistics' }},
      queryActivityStatistics: { method: 'GET', isArray: true, params: { action: 'statistics' }},
      getBpmn20Xml: { method: 'GET', params: { action: 'xml' }, cache: true }
    });
  }];

  module.factory('ProcessDefinitionResource', Resource);

});