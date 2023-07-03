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

var template = require('./cam-tasklist-task-detail-diagram-plugin.html?raw');

var Controller = [
  '$scope',
  '$q',
  'camAPI',
  function($scope, $q, camAPI) {
    // setup ///////////////////////////////////////////////////////////

    var ProcessDefinition = camAPI.resource('process-definition');
    var CaseDefinition = camAPI.resource('case-definition');
    var diagramData = $scope.taskData.newChild($scope);

    // provider ////////////////////////////////////////////////////////

    diagramData.provide('xml', [
      'processDefinition',
      'caseDefinition',
      function(processDefinition, caseDefinition) {
        if (!processDefinition && !caseDefinition) {
          return $q.when(null);
        }

        if (processDefinition) {
          return getDefinition($q, ProcessDefinition, processDefinition)
            .then(function(xml) {
              return xml.bpmn20Xml;
            })
            .catch(function() {});
        }

        return getDefinition($q, CaseDefinition, caseDefinition)
          .then(function(xml) {
            return xml.cmmnXml;
          })
          .catch(function() {});
      }
    ]);

    diagramData.provide('diagram', [
      'xml',
      'task',
      'caseDefinition',
      'processDefinition',
      function(xml, task, caseDefinition, processDefinition) {
        return {
          xml: xml,
          task: task,
          definition: processDefinition || caseDefinition
        };
      }
    ]);

    // observer /////////////////////////////////////////////////////////

    diagramData.observe('processDefinition', function(processDefinition) {
      $scope.processDefinition = processDefinition;
    });

    diagramData.observe('caseDefinition', function(caseDefinition) {
      $scope.caseDefinition = caseDefinition;
    });

    $scope.diagramState = diagramData.observe('diagram', function(diagram) {
      $scope.diagram = diagram;
    });

    $scope.control = {};

    $scope.highlightTask = function() {
      $scope.control.scrollToElement($scope.diagram.task.taskDefinitionKey);
      $scope.control.highlight($scope.diagram.task.taskDefinitionKey);
    };
  }
];

function getDefinition($q, DefinitionApi, definition) {
  var deferred = $q.defer();

  DefinitionApi.xml(definition, function(err, res) {
    if (err) {
      deferred.reject(err);
    } else {
      deferred.resolve(res);
    }
  });

  return deferred.promise;
}

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('tasklist.task.detail', {
    id: 'task-detail-diagram',
    label: 'DIAGRAM',
    template: template,
    controller: Controller,
    priority: 600
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
