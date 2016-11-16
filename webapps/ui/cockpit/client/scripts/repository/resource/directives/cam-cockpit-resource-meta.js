'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-resource-meta.html', 'utf8');

module.exports = [
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
      ) {

        // fields ////////////////////////////////////////////////////

          var resourceMetaData = $scope.resourceData.newChild($scope);
          $scope.isDmnResource = $scope.control.isDmnResource;


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
