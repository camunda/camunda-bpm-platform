define([
  'angular',
  'text!./cam-cockpit-definitions-plugin.html',
], function(
  angular,
  template
) {
  'use strict';

  var Controller = [
   '$scope',
   '$q',
   'camAPI',
  function (
    $scope,
    $q,
    camAPI
  ) {

    // fields //////////////////////////////////////////////////////

    var definitionsData = $scope.resourceData.newChild($scope);

    var isBpmnResource = $scope.isBpmnResource = $scope.control.isBpmnResource;
    var isCmmnResource = $scope.isCmmnResource = $scope.control.isCmmnResource;
    var isDmnResource = $scope.isDmnResource = $scope.control.isDmnResource;

    var ProcessDefinition = camAPI.resource('process-definition');
    var ProcessInstance = camAPI.resource('process-instance');

    var CaseDefinition = camAPI.resource('case-definition');
    var CaseInstance = camAPI.resource('case-instance');

    var DecisionDefinition = camAPI.resource('decision-definition');

    var resource;


    // provide /////////////////////////////////////////////////////

    definitionsData.provide('definitions', [ 'currentDeployment', 'resource', function(deployment, resource) {
      var deferred = $q.defer();

      $scope.loadingState = 'LOADING';

      var Service = null;
      var bpmnResource = false;

      if (!deployment || !resource) {
        deferred.resolve([]);
      }
      else {
        if (isBpmnResource(resource)) {
          bpmnResource = true;
          Service = ProcessDefinition;
        }
        else if (isCmmnResource(resource)) {
          Service = CaseDefinition;
        }
        else if (isDmnResource(resource)) {
          Service = DecisionDefinition;
        }

        if (!Service) {
          deferred.resolve([]);
        }
        else {

          Service.list({
            deploymentId: deployment.id,
            resourceName: resource.name
          }, function(err, res) {

            if (err) {
              $scope.loadingState = 'ERROR';
              $scope.textError = err.message || 'Failed to load definitions.';
              return deferred.reject(err);
            }

            deferred.resolve(bpmnResource ? res.items : res);
          });
        }
      }

      return deferred.promise;
    }]);


    // observe //////////////////////////////////////////////////////

    definitionsData.observe('definitions', function(definitions) {
      $scope.loadingState = definitions && definitions.length ? 'LOADED' : 'EMPTY';
      $scope.definitions = definitions;

      if (definitions && definitions.length && !isDmnResource(resource)) {
        loadInstancesCount(definitions);
      }
    });

    definitionsData.observe('resource', function(_resource) {
      resource = $scope.resource = _resource;
    });


    // instances ///////////////////////////////////////////////////

    var loadInstancesCount = function(definitions) {

      function instancesCount(definition, query, Service) {

        definition.instances = {
          $loaded: false
        };

        Service.count(query, function(err, result) {

          if (err) {
            return definition.instances.$error = true;
          }

          definition.instances.$loaded = true;
          definition.instances.count = result.count;
        });
      }

      for (var i = 0, definition; !!(definition = definitions[i]); i++) {

        var Service = null;
        var query = null;

        if (isBpmnResource(definition.resource)) {
          Service = ProcessInstance;
          query = {
            processDefinitionId: definition.id
          };
        }
        else if (isCmmnResource(definition.resource)) {
          Service = CaseInstance;
          query = {
            caseDefinitionId: definition.id
          };
        }

        if (Service) {
          instancesCount(definition, query, Service);
        }
      }
    }


    // link ////////////////////////////////////////////////////////

    $scope.getDefinitionLink = function(definition, resource) {
      if (resource) {

        var path = null;

        if (isBpmnResource(resource)) {
          path = 'process-definition';
        }
        else if (isDmnResource(resource)) {
          path = 'decision-definition';
        }

        return '#/' + path + '/' + definition.id;
      }
    };

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.repository.resource.detail', {
      id: 'resource-details',
      label: 'Definitions',
      template: template,
      controller: Controller,
      priority: 1000
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
