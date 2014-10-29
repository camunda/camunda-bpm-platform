define([
  'text!./cam-tasklist-task-detail-diagram-plugin.html',
], function(
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

    // setup ///////////////////////////////////////////////////////////

    var ProcessDefinition = camAPI.resource('process-definition');
    var diagramData = $scope.taskData.newChild($scope);

    // provider ////////////////////////////////////////////////////////

    $scope.processDiagramState = diagramData.provide('processDiagram', ['bpmn20xml', 'processDefinition', 'task', function (bpmn20xml, processDefinition, task) {
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

  return Configuration;

});
