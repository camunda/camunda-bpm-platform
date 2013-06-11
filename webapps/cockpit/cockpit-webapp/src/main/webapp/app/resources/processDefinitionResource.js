'use strict';

ngDefine('cockpit.resources', function(module) {
  
  var ProcessDefinitionResource = function ($resource, Uri) {
    return $resource(Uri.appUri('engine://process-definition/:id'), { id: '@id' }, {
      queryStatistics: { method: 'GET', isArray: true, params: { id: 'statistics' }}
    });
  };

  var ProcessDefinitionActivityStatisticsResource = function ($resource, Uri) {
    return $resource(Uri.appUri('engine://process-definition/:id/statistics'), { id: '@id' }, {
      queryStatistics: { method: 'GET', isArray: true, params: { id: 'statistics' }}
    });
  };

  module
    .factory('ProcessDefinitionResource', ProcessDefinitionResource)
    .factory('ProcessDefinitionActivityStatisticsResource', ProcessDefinitionActivityStatisticsResource);

});
