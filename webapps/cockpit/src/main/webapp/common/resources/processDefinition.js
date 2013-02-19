angular
.module('cockpit.resource.process.defintion', ['ngResource'])
.factory('ProcessDefinition', function($resource, App) {
  return $resource(App.restUri('/process-definition/'), {id: '@id'}, {});
});