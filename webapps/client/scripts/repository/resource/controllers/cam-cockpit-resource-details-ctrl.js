define([], function() {
  'use strict';

  return [
    '$scope',
    '$q',
    'Uri',
    'camAPI',
    'Views',
  function(
    $scope,
    $q,
    Uri,
    camAPI,
    Views
  ) {

    // utilities ///////////////////////////////////////////////////

    var isObject = angular.isObject;

    var checkResource = function(name, pattern) {
      return name && pattern.test(name.toLowerCase());
    };


    // fields //////////////////////////////////////////////////////

    var resourceDetailsData = $scope.resourceDetailsData = $scope.repositoryData.newChild($scope);

    var Deployment = camAPI.resource('deployment');

    var control = $scope.control = {};

    var BPMN_PATTERN   = /\.(bpmn\d*.xml|bpmn)$/;
    var CMMN_PATTERN   = /\.(cmmn\d*.xml|cmmn)$/;
    var DMN_PATTERN    = /\.(dmn\d*.xml|dmn)$/;
    var IMAGE_PATTERN  = /\.(gif|jpg|jpeg|jpe|png|svg|tif|tiff)$/;

    var PLUGIN_ACTION_COMPONENT = 'cockpit.repository.resource.action';


    // type of a resource //////////////////////////////////////////

    var getResourceName = function(resource) {
      if (isObject(resource)) {
        return resource.name;
      }
      return resource;
    }

    var isBpmnResource = control.isBpmnResource = $scope.isBpmnResource = function(resource) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, BPMN_PATTERN);
    };

    var isCmmnResource = control.isCmmnResource = $scope.isCmmnResource = function(resource) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, CMMN_PATTERN);
    };

    var isDmnResource = control.isDmnResource = $scope.isDmnResource = function(resource) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, DMN_PATTERN);
    };

    var isImageResource = control.isImageResource = $scope.isImageResource = function(resource) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, IMAGE_PATTERN);
    };

    var isUnkownResource = control.isUnkownResource = $scope.isUnkownResource = function(resource) {
      return !isBpmnResource(resource) && !isCmmnResource(resource) && !isDmnResource(resource) && !isImageResource(resource);
    };


    // download link ////////////////////////////////////////////////

    var downloadLink = control.downloadLink = $scope.downloadLink = function(deployment, resource) {
      return deployment && resource && Uri.appUri('engine://engine/:engine/deployment/' + deployment.id + '/resources/' + resource.id + '/data');
    };

    // provide //////////////////////////////////////////////////////

    resourceDetailsData.provide('binary', [ 'resource', 'currentDeployment', function(resource, deployment) {
      var deferred = $q.defer();
      
      if (!resource) {
        deferred.resolve(null);
      }
      else if (!deployment || deployment.id === null) {
        deferred.resolve(null);
      }
      else if (isImageResource(resource)) {
        // do not load image twice
        deferred.resolve(null);
      }
      else {
        Deployment.getResourceData(deployment.id, resource.id, function(err, res) {
          if(err) {
            deferred.reject(err);
          }
          else {
            deferred.resolve(res);
          }

        });
      }

      return deferred.promise;
    }]);


    // observe /////////////////////////////////////////////////

    resourceDetailsData.observe('resource', function(resource) {
      $scope.resource = resource;
    });

    resourceDetailsData.observe('currentDeployment', function(deployment) {
      $scope.deployment = deployment;
    });


    // plugins //////////////////////////////////////////////////

    $scope.resourceVars = { read: [ 'control', 'deployment', 'resource', 'resourceDetailsData' ] };
    $scope.resourceActions = Views.getProviders({ component: PLUGIN_ACTION_COMPONENT });

  }];

});
