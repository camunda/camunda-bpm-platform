ngDefine('cockpit.resources', function(module) {

  var Resource = [ '$resource', 'Uri', function ($resource, Uri) {

    return $resource(Uri.appUri('engine://engine/:engine/execution/:executionId/localVariables/:localVariableName'), { }, {
      updateVariables : {method: 'POST'}
    });
  }];

  module.factory('LocalExecutionVariableResource', Resource);

});