'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-resource-content.html', 'utf8');

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

          var resourceContentData = $scope.resourceData.newChild($scope);

          var resource;

          $scope.isBpmnResource = $scope.control.isBpmnResource;
          $scope.isCmmnResource = $scope.control.isCmmnResource;
          $scope.isDmnResource = $scope.control.isDmnResource;
          $scope.isImageResource = $scope.control.isImageResource;
          $scope.isUnkownResource = $scope.control.isUnkownResource;
          $scope.imageLink = $scope.control.downloadLink;


        // observe //////////////////////////////////////////////////

          resourceContentData.observe('resource', function(_resource) {
            if (_resource && resource && _resource.id !== resource.id) {
              $scope.binary = null;
            }
            resource = $scope.resource = _resource;
          });

          resourceContentData.observe('currentDeployment', function(_deployment) {
            $scope.deployment = _deployment;
          });

          resourceContentData.observe('binary', function(binary) {
            $scope.binary = (binary || {}).data;
          });

        }
      ]};
  }];
