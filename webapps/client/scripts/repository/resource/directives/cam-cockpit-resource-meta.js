define([
  'angular',
  'text!./cam-cockpit-resource-meta.html'
], function(
  angular,
  template
) {
  'use strict';

  return [
    '$modal',
    'camAPI',
  function(
    $modal,
    camAPI
  ) {

    return {
      scope: {
        resourceData: '='
      },

      template: template,

      controller: [
        '$scope',
      function(
        $scope
      ){

        var resourceMetaData = $scope.resourceData.newChild($scope);

        resourceMetaData.observe('resource', function(resource) {
          $scope.resource = resource;
        });

        $scope.state = resourceMetaData.observe('binary', function(binary) {
          $scope.binary = binary;
        });

      }
    ]};
  }];
});
