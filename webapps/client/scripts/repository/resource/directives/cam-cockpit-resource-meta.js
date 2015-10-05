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
          $scope.resource = resource;
        });

      }
    ]};
  }];
});
