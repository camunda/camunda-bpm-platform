'use strict';

var fs = require('fs');
var angular = require('angular');

var incidentsTemplate = fs.readFileSync(__dirname + '/incidents-tab.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
    id: 'incidents-tab',
    label: 'Incidents',
    template: incidentsTemplate,
    controller: [
      '$scope', '$http', '$modal', 'search', 'Uri', 'Views',
      function($scope,   $http,   $modal,   search,   Uri, Views) {

        // input: processInstance, processData

        var incidentData = $scope.processData.newChild($scope);
        var processInstance = $scope.processInstance;

        var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };

        var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

        var filter = null;

        $scope.$watch('pages.current', function(newValue, oldValue) {
          if (newValue == oldValue) {
            return;
          }

          search('page', !newValue || newValue == 1 ? null : newValue);
        });

        incidentData.observe([ 'filter', 'bpmnElements', 'activityIdToInstancesMap' ], function(newFilter, bpmnElements, activityIdToInstancesMap) {
          pages.current = newFilter.page || 1;

          updateView(newFilter, bpmnElements, activityIdToInstancesMap);
        });

        function updateView(newFilter, bpmnElements, activityIdToInstancesMap) {
          filter = angular.copy(newFilter);

          delete filter.page;
          delete filter.activityInstanceIds;
          delete filter.scrollToBpmnElement;

          var page = pages.current,
              count = pages.size,
              firstResult = (page - 1) * count;

          var defaultParams = {
            processInstanceIdIn: [ processInstance.id ]
          };

          var pagingParams = {
            firstResult: firstResult,
            maxResults: count
          };

          var params = angular.extend({}, filter, defaultParams);

          // fix missmatch -> activityIds -> activityIdIn
          params.activityIdIn = params.activityIds;
          delete params.activityIds;

          $scope.incidents = null;

          // get the 'count' of incidents
          $http.post(Uri.appUri('plugin://base/:engine/incident/count'), params).success(function(data) {
            pages.total = data.count;
          });

          // get the incidents
          $scope.loadingState = 'LOADING';
          $http.post(Uri.appUri('plugin://base/:engine/incident'), params, {params: pagingParams }).success(function(data) {
            angular.forEach(data, function(incident) {
              var activityId = incident.activityId;
              var bpmnElement = bpmnElements[activityId];
              incident.activityName = (bpmnElement && (bpmnElement.name || bpmnElement.id)) || activityId;
              incident.linkable = bpmnElements[activityId] && activityIdToInstancesMap[activityId] && activityIdToInstancesMap[activityId].length > 0;
            });

            $scope.incidents = data;
            $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';
          });
        }

        $scope.getIncidentType = function(incident) {
          if (incident.incidentType === 'failedJob') {
            return 'Failed Job';
          }

          return incident.incidentType;
        };

        $scope.getJobStacktraceUrl = function(incident) {
          return Uri.appUri('engine://engine/:engine/job/' + incident.rootCauseIncidentConfiguration + '/stacktrace');
        };

        $scope.incidentVars = { read: ['incident', 'processData', 'filter']};
        $scope.incidentActions = Views.getProviders({ component: 'cockpit.incident.action' });
      }],
    priority: 15
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
