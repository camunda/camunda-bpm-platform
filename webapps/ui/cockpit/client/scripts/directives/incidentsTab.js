'use strict';

var angular = require('angular');
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/incidents-tab.html', 'utf8');

var Directive = [
  '$http', '$q', '$uibModal', 'search', 'Uri', 'Views', '$translate', 'localConf',
  function($http, $q, $modal, search, Uri, Views, $translate, localConf) {

    var Link = function linkFunction(scope) {

      // ordered available columns
      var availableColumns = [
        { class: 'state',      request: 'incidentState',     sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_STATE')},
        { class: 'message',    request: '',                  sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_MESSAGE')},
        { class: 'process-instance', request: 'processInstanceId', sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_MESSAGE_PROCESS_INSTANCE')},
        { class: 'create-time',request: 'createTime',        sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_CREATE_TIME')},
        { class: 'end-time',   request: 'endTime',           sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_END_TIME')},
        { class: 'timestamp',  request: 'incidentTimestamp', sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_TIMESTAMP')},
        { class: 'activity',   request: 'activityId',        sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_ACTIVITY')},
        { class: 'cause instance-id uuid',      request: 'causeIncidentProcessInstanceId',     sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_CAUSE_INSTANCE_ID')},
        { class: 'cause-root instance-id uuid', request: 'rootCauseIncidentProcessInstanceId', sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_CAUSE_ROOT_INSTANCE_ID')},
        { class: 'type',       request: 'incidentType',      sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_TYPE')},
        { class: 'action',     request: '',                  sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_ACTION')}
      ];

      scope.onSortChange = updateView;

      // filter table column based on the view level (definition | instance | history | runtime)
      var classesToInclude = ['activity', 'cause instance-id uuid', 'cause-root instance-id uuid', 'type', 'action'];
      var PInstanceClass = scope.processDefinition && 'process-instance';
      if(scope.incidentsContext === 'history') {
        scope.localConfKey = 'sortHistInci';
        classesToInclude = ['state', 'message', PInstanceClass, 'create-time', 'end-time'].concat(classesToInclude);
      } else {
        scope.localConfKey = 'sortInci';
        classesToInclude = ['message', PInstanceClass, 'timestamp'].concat(classesToInclude);
      }

      scope.headColumns = availableColumns.filter(function(column) {
        return classesToInclude.indexOf(column.class) !== -1;
      });


      scope.sortObj = loadLocal({ sortBy: 'incidentType', sortOrder: 'asc' });

      var incidentData = scope.processData.newChild(scope);

      var pages = scope.pages = angular.copy({size: 50, total: 0, current: 1});

      var baseRuntimeUrl = 'plugin://base/:engine/incident/';
      var baseHistoricUrl = 'plugin://history/:engine/incident/';

      scope.incidents = null;

      scope.onPaginationChange = function onPaginationChange(pages) {
        scope.pages.current = pages.current;
        updateView();
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

      function updateView(query, pages, sortObj) {
        scope.sortObj = sortObj || scope.sortObj;

        if (!scope.filter || !scope.bpmnElements || !scope.activityIdToInstancesMap) {
          // return empty promise
          return $q.when(null);
        }

        if (pages) {
          scope.pages = pages;
        }

        var newFilter = scope.filter;
        var bpmnElements = scope.bpmnElements;
        var activityIdToInstancesMap = scope.activityIdToInstancesMap;

        var filter = angular.copy(newFilter);

        delete filter.page;
        delete filter.activityInstanceIds;
        delete filter.scrollToBpmnElement;

        var page =  scope.pages.current,
            count =  scope.pages.size,
            firstResult = (page - 1) * count,
            queryParams = query || {};

        var defaultParams;

        if (scope.processDefinition) {
          defaultParams = {processDefinitionIdIn: [scope.processDefinition.id]};
        } else {
          defaultParams = {processInstanceIdIn: [scope.processInstance.id]};
        }

        // Add default sorting param
        if (sortObj) {
          defaultParams.sortBy = scope.sortObj.sortBy;
          defaultParams.sortOrder = scope.sortObj.sortOrder;
          saveLocal(sortObj);
        }

        var pagingParams = {
          firstResult: firstResult,
          maxResults: count
        };

        var params = angular.extend({}, filter, defaultParams, queryParams);

        params.activityIdIn = params.activityIds;
        delete params.activityIds;

        var baseUrl = scope.incidentsContext === 'history' ? baseHistoricUrl : baseRuntimeUrl;

        // get the 'count' of incidents
        $http.post(Uri.appUri(baseUrl + 'count'), params).then(function(response) {
          scope.pages.total = response.data.count;
        });

        // get the incidents
        scope.loadingState = 'LOADING';
        return $http.post(Uri.appUri(baseUrl), params, {params: pagingParams})
          .then(function(res) {
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

      scope.incidentHasActions = function(incident) {
        return scope.incidentsContext !== 'history' ||
          scope.incidentsContext === 'history' &&
          incident.incidentType === 'failedJob' &&
          !incident.deleted &&
          !incident.resolved;
      };
      scope.incidentVars = { read: ['incident', 'processData', 'filter']};
      scope.incidentActions = Views.getProviders({ component: 'cockpit.incident.action' });


      function saveLocal(sortObj) {
        localConf.set(scope.localConfKey, sortObj);
      }

      function loadLocal(defaultValue) {
        return localConf.get(scope.localConfKey, defaultValue);
      }
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
