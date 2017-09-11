'use strict';

var angular = require('angular');
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/incidents-tab.html', 'utf8');


var Directive = [
  '$http', '$q', '$modal', 'search', 'Uri', 'Views',
  function($http,  $q,   $modal,   search,   Uri, Views) {

    var Link =  function linkFunction(scope) {

      var incidentData = scope.processData.newChild(scope);

      var pages = scope.pages = angular.copy({ size: 50, total: 0, current: 1 });

      var baseRuntimeUrl = 'plugin://base/:engine/incident/';
      var baseHistoricUrl = 'plugin://history/:engine/incident/';

      scope.incidents = null;

      scope.onPaginationChange = function onPaginationChange(pages) {
        scope.pages = pages;
      };

      scope.getIncidentState = function(incident) {
        var result = 'open';

        if (incident.resolved) {
          result = 'resolved';
        } else if (incident.deleted) {
          result = 'deleted';
        }

        return result;
      };


      incidentData.observe(
        ['filter', 'bpmnElements', 'activityIdToInstancesMap'],
        function(newFilter, bpmnElements, activityIdToInstancesMap) {
          pages.current = newFilter.page || 1;
          scope.filter = newFilter;
          scope.bpmnElements = bpmnElements;
          scope.activityIdToInstancesMap = activityIdToInstancesMap;
          updateView();
        });

      function updateView(sq, newPages) {
        if(!scope.filter || !scope.bpmnElements || !scope.activityIdToInstancesMap) {
          // return empty promise
          return $q.when(null);
        }

        var searchQuery = sq || {};

        if(newPages) {
          scope.pages = newPages;
        }

        var newFilter = scope.filter;
        var bpmnElements = scope.bpmnElements;
        var activityIdToInstancesMap = scope.activityIdToInstancesMap;
        searchQuery = searchQuery || {};

        var filter = angular.copy(newFilter);

        delete filter.page;
        delete filter.activityInstanceIds;
        delete filter.scrollToBpmnElement;

        var page = scope.pages.current,
            count = scope.pages.size,
            firstResult = (page - 1) * count;

        var defaultParams;

        if(scope.processDefinition) {
          defaultParams = { processDefinitionIdIn: [scope.processDefinition.id] };
        } else {
          defaultParams = { processInstanceIdIn: [scope.processInstance.id] };
        }

        var pagingParams = {
          firstResult: firstResult,
          maxResults: count
        };

        var params = angular.extend({}, filter, defaultParams, searchQuery);

        params.activityId = params.activityIds;
        delete params.activityIds;

        var baseUrl = scope.incidentsContext === 'history' ? baseHistoricUrl : baseRuntimeUrl;

          // get the 'count' of incidents
        $http.post(Uri.appUri(baseUrl+'count'), params).success(function(data) {
          scope.pages.total = data.count;
        });

          // get the incidents
        scope.loadingState = 'LOADING';
        return $http.post(Uri.appUri(baseUrl), params, {params: pagingParams }).then(function(res) {
          var data = res.data;
          angular.forEach(data, function(incident) {
            var activityId = incident.activityId;
            var bpmnElement = bpmnElements[activityId];
            incident.activityName = (bpmnElement && (bpmnElement.name || bpmnElement.id)) || activityId;
            incident.linkable = bpmnElements[activityId] && activityIdToInstancesMap[activityId] && activityIdToInstancesMap[activityId].length > 0;
          });

          scope.incidents = data;
          scope.loadingState = data.length ? 'LOADED' : 'EMPTY';
        });

      }

      scope.getIncidentType = function(incident) {
        if (incident.incidentType === 'failedJob') {
          return 'Failed Job';
        }

        return incident.incidentType;
      };

      scope.getJobStacktraceUrl = function(incident) {
        return Uri.appUri('engine://engine/:engine/job/' + incident.rootCauseIncidentConfiguration + '/stacktrace');
      };

      scope.incidentVars = { read: ['incident', 'processData', 'filter']};
      scope.incidentActions = Views.getProviders({ component: 'cockpit.incident.action' });
    };

    return {
      restrict: 'E',
      scope: {
        processData: '=',
        processDefinition: '=',
        processInstance: '=',
        incidentsContext: '@'
      },
      template: template,
      link: Link
    };
  }];




module.exports = Directive;
