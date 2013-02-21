angular
.module('cockpit.resource.process.defintion', ['ngResource'])
.factory('ProcessDefinitionResource', function($resource, Uri) {
  return $resource(Uri.restUri('/process-definition/:id'), {id: '@id'}, {
    'queryStatistics':  {method:'GET', isArray:true, params: { id: 'statistics' }}
  });
});