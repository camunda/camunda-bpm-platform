angular
.module('cockpit.resource.process.instance', ['ngResource'])
.factory('ProcessInstanceResource', function($resource, Uri) {
  return $resource(Uri.restUri('/process-instance/:id'), {id: '@id'}, {
    'count':  {method:'GET', isArray:true, params: { id: 'count' }}
  });
});