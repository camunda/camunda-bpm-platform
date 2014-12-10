define(['text!./job-definition-suspension-overlay.html'], function(template) {

  var Controller = [ '$scope', function ($scope) {

    var bpmnElement = $scope.bpmnElement,
        processData = $scope.processData.newChild($scope);

    processData.provide('jobDefinition', ['jobDefinitions', function (jobDefinitions) {
      for(var i = 0; i < jobDefinitions.length; i++) {
        var jobDefinition = jobDefinitions[i];
        if (jobDefinition.activityId === bpmnElement.id) {
          return jobDefinition;
        }
      }
      return null;
    }]);

    $scope.jobDefinition = processData.observe('jobDefinition', function(jobDefinition) {
      if (jobDefinition) {
        bpmnElement.isSelectable = true;
      }
      $scope.jobDefinition = jobDefinition;
    });

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

  return Configuration;
});
