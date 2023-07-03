/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var template = require('./cam-cockpit-definitions-plugin.html?raw');

var Controller = [
  '$scope',
  '$q',
  'camAPI',
  'hasPlugin',
  function($scope, $q, camAPI, hasPlugin) {
    // fields //////////////////////////////////////////////////////

    var definitionsData = $scope.resourceData.newChild($scope);

    var isBpmnResource = ($scope.isBpmnResource =
      $scope.control.isBpmnResource);
    var isCmmnResource = ($scope.isCmmnResource =
      $scope.control.isCmmnResource);
    var isDmnResource = ($scope.isDmnResource = $scope.control.isDmnResource);

    var ProcessInstance = camAPI.resource('process-instance');
    var CaseInstance = camAPI.resource('case-instance');
    var DrdService = camAPI.resource('drd');

    var resource;

    $scope.hasCasePlugin = hasPlugin(
      'cockpit.cases.dashboard',
      'case-definition'
    );
    $scope.hasDrdPlugin = hasPlugin(
      'cockpit.drd.definition.tab',
      'decision-instance-table'
    );

    // observe //////////////////////////////////////////////////////

    $scope.pages = {current: 1, size: 50, total: 0};

    $scope.onPaginationChange = function() {
      definitionsData.changed('definitions');
      $scope.loadingState = 'LOADING';
    };

    definitionsData.observe('resource', function() {
      $scope.pages.current = 1;
      $scope.loadingState = 'LOADING';
    });

    definitionsData.observe(['definitions', 'pages'], function(
      definitions,
      pages
    ) {
      $scope.pages = pages;

      $scope.loadingState =
        definitions && definitions.length ? 'LOADED' : 'EMPTY';
      $scope.definitions = definitions;

      if (definitions && definitions.length && !isDmnResource(resource)) {
        loadInstancesCount(definitions);
      }

      if (isDmnResource(resource)) {
        loadDecisionRequirementsDefinition(definitions);
      }
    });

    definitionsData.observe('resource', function(_resource) {
      resource = $scope.resource = _resource;
      $scope.hasDefinitions =
        isBpmnResource(resource) ||
        isCmmnResource(resource) ||
        isDmnResource(resource);
    });

    // instances ///////////////////////////////////////////////////

    var loadInstancesCount = function(definitions) {
      function instancesCount(definition, query, Service) {
        definition.instances = {
          $loaded: false
        };

        Service.count(query, function(err, result) {
          if (err) {
            return (definition.instances.$error = true);
          }

          definition.instances.$loaded = true;
          definition.instances.count = !isNaN(+result)
            ? result
            : result.count || '0';

          var phase = $scope.$root.$$phase;
          if (phase !== '$apply' && phase !== '$digest') {
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
        } else if (isCmmnResource(definition.resource)) {
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
            return ($scope.drdTextError = err);
          }

          $scope.drdLoadingState = 'LOADED';
          $scope.drd = result;

          var phase = $scope.$root.$$phase;
          if (phase !== '$apply' && phase !== '$digest') {
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
        } else if (isDmnResource(resource)) {
          path = 'decision-definition';
        } else if (isCmmnResource(resource)) {
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
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.repository.resource.detail', {
    id: 'resource-details',
    template: template,
    controller: Controller,
    priority: 1000
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
