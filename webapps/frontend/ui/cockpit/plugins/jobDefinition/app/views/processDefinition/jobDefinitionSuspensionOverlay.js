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

var template = require('./job-definition-suspension-overlay.html?raw');

var Controller = [
  '$scope',
  function($scope) {
    var bpmnElement = $scope.bpmnElement,
      processData = $scope.processData.newChild($scope);

    processData.provide('jobDefinitionsForElement', [
      'jobDefinitions',
      function(jobDefinitions) {
        var matchedDefinitions = [];
        for (var i = 0; i < jobDefinitions.length; i++) {
          var jobDefinition = jobDefinitions[i];
          if (jobDefinition.activityId === bpmnElement.id) {
            matchedDefinitions.push(jobDefinition);
          }
        }
        return matchedDefinitions;
      }
    ]);

    $scope.$on('$processDefinition.suspensionState.changed', function() {
      processData.changed('jobDefinitions');
    });

    $scope.jobDefinitionsForElement = processData.observe(
      'jobDefinitionsForElement',
      function(jobDefinitionsForElement) {
        if (jobDefinitionsForElement.length > 0) {
          bpmnElement.isSelectable = true;
        }
        $scope.jobDefinitionsForElement = jobDefinitionsForElement;
      }
    );

    $scope.isSuspended = function() {
      return (
        $scope.jobDefinitionsForElement.filter &&
        $scope.jobDefinitionsForElement.filter(function(jobDefinition) {
          return jobDefinition.suspended;
        }).length > 0
      );
    };
  }
];

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView(
    'cockpit.processDefinition.diagram.overlay',
    {
      id: 'job-definition-diagram-overlay',
      template: template,
      controller: Controller,
      priority: 10
    }
  );
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
