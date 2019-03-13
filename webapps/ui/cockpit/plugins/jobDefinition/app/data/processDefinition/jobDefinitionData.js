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

var angular = require('angular');

var Controller = [ '$scope', 'processData', 'JobDefinitionResource',
  function($scope, processData, JobDefinitionResource) {

    $scope.$on('$processDefinition.suspensionState.changed', function() {
      processData.changed('jobDefinitions');
    });

    processData.provide('jobDefinitions', ['processDefinition', function(processDefinition) {
      return JobDefinitionResource.query({ processDefinitionId : processDefinition.id }).$promise;
    }]);

    processData.observe(['jobDefinitions', 'bpmnElements'], function(jobDefinitions, bpmnElements) {

      angular.forEach(jobDefinitions, function(jobDefinition) {
        var activityId = jobDefinition.activityId,
            bpmnElement = bpmnElements[activityId];

        jobDefinition.activityName = (bpmnElement && (bpmnElement.name || bpmnElement.id)) || activityId;

      });

    });

  }];

var Configuration = function PluginConfiguration(DataProvider) {

  DataProvider.registerData('cockpit.processDefinition.data', {
    id: 'job-definitions-data',
    controller: Controller
  });
};

Configuration.$inject = ['DataProvider'];

module.exports = Configuration;
