ngDefine('cockpit.resources', function(module) {

  var Resource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/task/:id/:action'), { id: '@id' }, {
      query: { method: 'POST', isArray: true},
      count: { method: 'POST', isArray: false, params: { id: 'count' }},
      setAssignee: { method: 'POST', params: { action: 'assignee' }}
    });
  }];

  module.factory('TaskResource', Resource);

});
