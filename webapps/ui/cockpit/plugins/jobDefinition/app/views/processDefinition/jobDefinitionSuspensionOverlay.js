'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/job-definition-suspension-overlay.html', 'utf8');

var Controller = [ '$scope', function($scope) {

  var bpmnElement = $scope.bpmnElement,
      processData = $scope.processData.newChild($scope);

  processData.provide('jobDefinitionsForElement', ['jobDefinitions', function(jobDefinitions) {
    var matchedDefinitions = [];
    for(var i = 0; i < jobDefinitions.length; i++) {
      var jobDefinition = jobDefinitions[i];
      if (jobDefinition.activityId === bpmnElement.id) {
        matchedDefinitions.push(jobDefinition);
      }
    }
    return matchedDefinitions;
  }]);

  $scope.jobDefinitionsForElement = processData.observe('jobDefinitionsForElement', function(jobDefinitionsForElement) {
    if (jobDefinitionsForElement.length > 0) {
      bpmnElement.isSelectable = true;
    }
    $scope.jobDefinitionsForElement = jobDefinitionsForElement;
  });

  $scope.isSuspended = function() {
    return $scope.jobDefinitionsForElement.filter &&
             $scope.jobDefinitionsForElement.filter(function(jobDefinition) {
               return jobDefinition.suspended;
             }).length > 0;
  };

}];

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.processDefinition.diagram.overlay', {
    id: 'job-definition-diagram-overlay',
    template: template,
    controller: Controller,
    priority: 10
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
