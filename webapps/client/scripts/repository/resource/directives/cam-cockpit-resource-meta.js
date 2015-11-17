define([
  'angular',
  'text!./cam-cockpit-resource-meta.html'
], function(
  angular,
  template
) {
  'use strict';

  return [
  function() {

    return {
      scope: {
        resourceData: '=',
        control: '='
      },

      template: template,

      controller: [
        '$scope',
      function(
        $scope
      ){

        // fields ////////////////////////////////////////////////////

        var resourceMetaData = $scope.resourceData.newChild($scope);


        // observe //////////////////////////////////////////////////

        resourceMetaData.observe('resource', function(resource) {
          if (resource) {
            var parts = (resource.name || resource.id).split('/');
            resource._filename = parts.pop();
            resource._filepath = parts.join('/');
          }

          $scope.resource = resource;
        });

        resourceMetaData.observe('definitions', function(definitions) {
          $scope.definitions = definitions;
        });

      }
    ]};
  }];
});
