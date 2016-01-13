'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-task-detail-diagram-plugin.html', 'utf8');

  var Controller = [
   '$scope',
   '$q',
   'camAPI',
  function (
    $scope,
    $q,
    camAPI
  ) {

    // setup ///////////////////////////////////////////////////////////

    var ProcessDefinition = camAPI.resource('process-definition');
    var diagramData = $scope.taskData.newChild($scope);

    // provider ////////////////////////////////////////////////////////

    diagramData.provide('bpmn20xml', ['processDefinition', function (processDefinition) {
      var deferred = $q.defer();

      if (!processDefinition) {
        return deferred.resolve(null);
      }

      ProcessDefinition.xml(processDefinition, function(err, res) {
        if(err) {
          deferred.reject(err);
        }
        else {
          deferred.resolve(res);
        }
      });

      return deferred.promise;
    }]);

    diagramData.provide('processDiagram', ['bpmn20xml', 'processDefinition', 'task', function (bpmn20xml, processDefinition, task) {
      var processDiagram = {};

      processDiagram.processDefinition = processDefinition;
      processDiagram.task = task;
      processDiagram.bpmn20xml = (bpmn20xml || {}).bpmn20Xml;

      return processDiagram;
    }]);

    // observer /////////////////////////////////////////////////////////

    diagramData.observe('processDefinition', function (processDefinition) {
      $scope.processDefinition = processDefinition;
    });

    $scope.processDiagramState = diagramData.observe('processDiagram', function (processDiagram) {
      $scope.processDiagram = processDiagram;
    });

    $scope.control = {};

    $scope.highlightTask = function() {
      $scope.control.highlight($scope.processDiagram.task.taskDefinitionKey);
    };

  }];

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
