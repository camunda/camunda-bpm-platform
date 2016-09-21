'use strict';

var fs = require('fs');
var angular = require('angular');
var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');

var template = fs.readFileSync(__dirname + '/job-definition-table.html', 'utf8');

var Controller = [
  '$scope',
  'Views',
  function($scope, Views) {

    var processData = $scope.processData.newChild($scope);

    processData.observe([ 'filter', 'jobDefinitions', 'bpmnElements' ], function(filter, jobDefinitions) {
      updateView(filter, jobDefinitions);
    });

    function updateView(filter, jobDefinitions) {

      $scope.jobDefinitions = null;

      var activityIds = filter.activityIds;

      if (!activityIds || !activityIds.length) {
        $scope.jobDefinitions = jobDefinitions;
        return;
      }

      var jobDefinitionSelection = [];

      angular.forEach(jobDefinitions, function(jobDefinition) {

        var activityId = jobDefinition.activityId;

        if (activityIds.indexOf(activityId) != -1) {
          jobDefinitionSelection.push(jobDefinition);
        }

      });

      $scope.jobDefinitions = jobDefinitionSelection;

    }

    $scope.jobDefinitionVars = { read: [ 'jobDefinition', 'processData', 'filter' ] };
    $scope.jobDefinitionActions = Views.getProviders({ component: 'cockpit.jobDefinition.action' });

    $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(null, 'activityIdIn');
  }];

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
    id: 'job-definition-table',
    label: 'Job Definitions',
    template: template,
    controller: Controller,
    priority: 2
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
