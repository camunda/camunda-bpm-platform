'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-definitions-plugin.html', 'utf8');

var Controller = [
  '$scope',
  '$q',
  'camAPI',
  'hasPlugin',
  function(
    $scope,
    $q,
    camAPI,
    hasPlugin
  ) {

    // fields //////////////////////////////////////////////////////

    var definitionsData = $scope.resourceData.newChild($scope);

    var isBpmnResource = $scope.isBpmnResource = $scope.control.isBpmnResource;
    var isCmmnResource = $scope.isCmmnResource = $scope.control.isCmmnResource;
    var isDmnResource = $scope.isDmnResource = $scope.control.isDmnResource;

    var ProcessInstance = camAPI.resource('process-instance');
    var CaseInstance = camAPI.resource('case-instance');
    var DrdService = camAPI.resource('drd');

    var resource;

    $scope.hasCasePlugin = hasPlugin('cockpit.cases.dashboard', 'case-definition');
    $scope.hasDrdPlugin = hasPlugin('cockpit.drd.definition.tab', 'decision-instance-table');

    // observe //////////////////////////////////////////////////////

    definitionsData.observe('definitions', function(definitions) {
      $scope.loadingState = definitions && definitions.length ? 'LOADED' : 'EMPTY';
      $scope.definitions = definitions;

      if (definitions && definitions.length && !isDmnResource(resource)) {
        loadInstancesCount(definitions);
      }

      if(isDmnResource(resource)) {
        loadDecisionRequirementsDefinition(definitions);
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
          definition.instances.count = result.count || '0';

          var phase = $scope.$root.$$phase;
          if(phase !== '$apply' && phase !== '$digest') {
            $scope.$apply();
          }

        });
      }

      for (var i = 0, definition; (definition = definitions[i]); i++) {

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
    };


    // drd //////////////////////////////////////////////////////////
    var loadDecisionRequirementsDefinition = function(definitions) {
      var drdId = definitions[0].decisionRequirementsDefinitionId;

      $scope.drdLoadingState = drdId ? 'LOADING' : 'EMPTY';

      if (drdId) {
        DrdService.get(drdId, function(err, result) {
          if (err) {
            return $scope.drdTextError = err;
          }

          $scope.drdLoadingState = 'LOADED';
          $scope.drd = result;

          var phase = $scope.$root.$$phase;
          if(phase !== '$apply' && phase !== '$digest') {
            $scope.$apply();
          }
        });
      }
    };


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
        else if (isCmmnResource(resource)) {
          path = 'case-definition';
        }

        return '#/' + path + '/' + definition.id;
      }
    };

    $scope.getDrdLink = function(definition) {
      if (definition) {
        return '#/decision-requirement/' + definition.id;
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

module.exports = Configuration;
