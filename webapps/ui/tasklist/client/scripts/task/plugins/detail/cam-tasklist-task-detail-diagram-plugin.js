'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-task-detail-diagram-plugin.html', 'utf8');

var Controller = [
  '$scope',
  '$q',
  'camAPI',
  function(
    $scope,
    $q,
    camAPI
  ) {

    // setup ///////////////////////////////////////////////////////////

    var ProcessDefinition = camAPI.resource('process-definition');
    var CaseDefinition = camAPI.resource('case-definition');
    var diagramData = $scope.taskData.newChild($scope);

    // provider ////////////////////////////////////////////////////////

    diagramData.provide('xml', ['processDefinition', 'caseDefinition', function(processDefinition, caseDefinition) {
      if (!processDefinition && !caseDefinition) {
        return $q.when(null);
      }

      if (processDefinition) {
        return getDefinition($q, ProcessDefinition, processDefinition)
          .then(function(xml) {
            return xml.bpmn20Xml;
          });
      }

      return getDefinition($q, CaseDefinition, caseDefinition)
        .then(function(xml) {
          return xml.cmmnXml;
        });
    }]);

    diagramData.provide('diagram',
      ['xml', 'task', 'caseDefinition', 'processDefinition', function(xml, task, caseDefinition, processDefinition) {
        return {
          xml: xml,
          task: task,
          definition: processDefinition || caseDefinition
        };
      }]
    );

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
      $scope.control.highlight($scope.diagram.task.taskDefinitionKey);
    };
  }];

function getDefinition($q, DefinitionApi, definition) {
  var deferred = $q.defer();

  DefinitionApi.xml(definition, function(err, res) {
    if(err) {
      deferred.reject(err);
    }
    else {
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
