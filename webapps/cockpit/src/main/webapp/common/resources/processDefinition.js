angular
.module('cockpit.resource.process.defintion', ['ngResource'])
.factory('ProcessDefinition', function($resource, App) {
  return $resource(App.restUri('/process-definition/:id'), {id: '@id'}, {
    'queryStatistics':  {method:'GET', isArray:true, params: { id: 'statistics' }}
  });
});