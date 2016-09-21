'use strict';

var fs = require('fs');
var angular = require('angular');
var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');

var template = fs.readFileSync(__dirname + '/called-process-definition-table.html', 'utf8');


module.exports = [ 'ViewsProvider', function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
    id: 'call-process-definitions-table',
    label: 'Called Process Definitions',
    template: template,
    controller: [
      '$scope', '$location', '$q', 'PluginProcessDefinitionResource',
      function($scope, $location, $q, PluginProcessDefinitionResource) {

        var filter;
        var processData = $scope.processData.newChild($scope);

        $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(null, 'activityIdIn');

        processData.provide('calledProcessDefinitions', [
          'processDefinition', 'filter',
          function(processDefinition,   newFilter) {

            filter = angular.copy(newFilter);

            delete filter.page;
            delete filter.scrollToBpmnElement;

          // the parent process definition id is the super process definition id...
            filter.superProcessDefinitionId = filter.parentProcessDefinitionId;
          // ...and the process definition id of the current view is the
          // parent process definition id of query.
            filter.parentProcessDefinitionId = $scope.processDefinition.id;

            filter.activityIdIn = filter.activityIds;
            delete filter.activityIds;

            $scope.loadingState = 'LOADING';
            return PluginProcessDefinitionResource.getCalledProcessDefinitions({ id: processDefinition.id }, filter).$promise;
          }]);

        processData.observe([ 'calledProcessDefinitions', 'bpmnElements' ], function(calledProcessDefinitions, bpmnElements) {

          $scope.calledProcessDefinitions = attachCalledFromActivities(calledProcessDefinitions, bpmnElements);
          $scope.loadingState = $scope.calledProcessDefinitions.length ? 'LOADED' : 'EMPTY';
        });

        function attachCalledFromActivities(processDefinitions, bpmnElements) {

          var result = [];

          angular.forEach(processDefinitions, function(d) {
            var calledFromActivityIds = d.calledFromActivityIds,
                calledFromActivities = [];

            angular.forEach(calledFromActivityIds, function(activityId) {
              var bpmnElement = bpmnElements[activityId];

              var activity = { id: activityId, name: (bpmnElement && bpmnElement.name) || activityId };

              calledFromActivities.push(activity);
            });

            result.push(angular.extend({}, d, { calledFromActivities: calledFromActivities }));
          });

          return result;
        }
      }],
    priority: 5
  });
}];
