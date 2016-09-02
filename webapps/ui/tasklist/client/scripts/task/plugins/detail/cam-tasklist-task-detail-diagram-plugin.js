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

    diagramData.provide('bpmn20xml', createXmlDefinitionProvider($q, ProcessDefinition, 'processDefinition'));
    diagramData.provide('cmmnXml', createXmlDefinitionProvider($q, CaseDefinition, 'caseDefinition'));

    diagramData.provide('caseDiagram', createDiagramProvider('cmmnXml', 'caseDefinition'));
    diagramData.provide('processDiagram', createDiagramProvider('bpmn20xml', 'processDefinition', 'bpmn20Xml'));

    // observer /////////////////////////////////////////////////////////

    diagramData.observe('processDefinition', function(processDefinition) {
      $scope.processDefinition = processDefinition;
    });

    diagramData.observe('caseDefinition', function(caseDefinition) {
      $scope.caseDefinition = caseDefinition;
    });

    $scope.processDiagramState = diagramData.observe('processDiagram', function(processDiagram) {
      $scope.processDiagram = processDiagram;
    });

    $scope.caseDiagramState = diagramData.observe('caseDiagram', function(caseDiagram) {
      $scope.caseDiagram = caseDiagram;
    });

    $scope.control = {};

    $scope.highlightTask = function() {
      if ($scope.processDefinition ) {
        $scope.control.highlight($scope.processDiagram.task.taskDefinitionKey);
      } else if ($scope.caseDefinition) {
        $scope.control.highlight($scope.caseDiagram.task.taskDefinitionKey);
      }
    };

  }];

// Creates new data provider for angular data depend. Provider should fetch xml of given definition.
function createXmlDefinitionProvider($q, DefinitionApi, definitionResource) {
  return [definitionResource, function(definition) {
    var deferred = $q.defer();

    if (!definition) {
      return deferred.resolve(null);
    }

    DefinitionApi.xml(definition, function(err, res) {
      if(err) {
        deferred.reject(err);
      }
      else {
        deferred.resolve(res);
      }
    });

    return deferred.promise;
  }];
}

// Creates new data provider for angular data depend. Provider should return the instance diagram.
function createDiagramProvider(xmlResource, definitionResource, xmlField) {
  xmlField = xmlField || xmlResource;

  return [xmlResource, definitionResource, 'task', function(xml, definition, task) {
    var diagram = {};

    diagram[definitionResource] = definition;
    diagram.task = task;
    diagram[xmlResource] = (xml || {})[xmlField];

    return diagram;
  }];
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
