/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var angular = require('angular');
var fs = require('fs');

var template = require('./incidents-tab.html')();
var inspectTemplate = require('./incidents-tab-stacktrace.html')();

var debouncePromiseFactory = require('camunda-bpm-sdk-js').utils
  .debouncePromiseFactory;
var debouncePromise = debouncePromiseFactory();

var Directive = [
  '$http',
  '$q',
  '$uibModal',
  'search',
  'Uri',
  'Views',
  '$translate',
  'localConf',
  '$location',
  'camAPI',
  function(
    $http,
    $q,
    $modal,
    search,
    Uri,
    Views,
    $translate,
    localConf,
    $location,
    camAPI
  ) {
    var Link = function linkFunction(scope) {
      // ordered available columns
      // prettier-ignore
      var availableColumns = [
        {class: 'state', request: 'incidentState', sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_STATE')},
        {class: 'message', request: 'incidentMessage', sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_MESSAGE')},
        {class: 'process-instance', request: 'processInstanceId', sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_MESSAGE_PROCESS_INSTANCE')},
        {class: 'create-time', request: 'createTime', sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_CREATE_TIME')},
        {class: 'end-time', request: 'endTime', sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_END_TIME')},
        {class: 'timestamp', request: 'incidentTimestamp', sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_TIMESTAMP')},
        {class: 'activity', request: 'activityId', sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_ACTIVITY')},
        {class: 'failedActivityId', request: 'failedActivityId', sortable: false, content: $translate.instant('PLGN_HIST_FAILING_ACTIVITY')},
        {class: 'cause instance-id uuid', request: 'causeIncidentProcessInstanceId', sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_CAUSE_INSTANCE_ID')},
        {class: 'cause-root instance-id uuid', request: 'rootCauseIncidentProcessInstanceId', sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_CAUSE_ROOT_INSTANCE_ID')},
        {class: 'type', request: 'incidentType', sortable: true, content: $translate.instant('PLUGIN_INCIDENTS_TAB_TYPE')},
        {class: 'annotation', request: '', sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_ANNOTATION')},
        {class: 'action', request: '', sortable: false, content: $translate.instant('PLUGIN_INCIDENTS_TAB_ACTION')}
      ];

      scope.onSortChange = updateView;

      // filter table column based on the view level (definition | instance | history | runtime)
      var classesToInclude = [
        'activity',
        'failedActivityId',
        'cause instance-id uuid',
        'cause-root instance-id uuid',
        'type',
        'annotation',
        'action'
      ];
      var PInstanceClass = scope.processDefinition && 'process-instance';
      if (scope.incidentsContext === 'history') {
        scope.localConfKey = 'sortHistInci';
        classesToInclude = [
          'state',
          'message',
          PInstanceClass,
          'create-time',
          'end-time'
        ].concat(classesToInclude);
      } else {
        scope.localConfKey = 'sortInci';
        classesToInclude = ['message', PInstanceClass, 'timestamp'].concat(
          classesToInclude
        );
      }

      scope.headColumns = availableColumns.filter(function(column) {
        return classesToInclude.indexOf(column.class) !== -1;
      });

      scope.sortObj = loadLocal({sortBy: 'incidentType', sortOrder: 'asc'});

      var incidentData = scope.processData.newChild(scope);

      var pages = (scope.pages = angular.copy({
        size: 50,
        total: 0,
        current: 1
      }));

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
        }
      );

      function updateView(query, pages, sortObj) {
        scope.sortObj = sortObj || scope.sortObj;

        if (
          !scope.filter ||
          !scope.bpmnElements ||
          !scope.activityIdToInstancesMap
        ) {
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

        var page = scope.pages.current,
          count = scope.pages.size,
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
          saveLocal(sortObj);
        }

        defaultParams.sortBy = scope.sortObj.sortBy;
        defaultParams.sortOrder = scope.sortObj.sortOrder;

        var pagingParams = {
          firstResult: firstResult,
          maxResults: count
        };

        var params = angular.extend({}, filter, defaultParams, queryParams);

        params.activityIdIn = params.activityIds;
        delete params.activityIds;

        var baseUrl =
          scope.incidentsContext === 'history'
            ? baseHistoricUrl
            : baseRuntimeUrl;

        // get the 'count' of incidents
        $http
          .post(Uri.appUri(baseUrl + 'count'), params)
          .then(function(response) {
            scope.pages.total = response.data.count;
          })
          .catch(angular.noop);

        // get the incidents
        scope.loadingState = 'LOADING';
        return debouncePromise(
          $http.post(Uri.appUri(baseUrl), params, {params: pagingParams})
        )
          .then(function(res) {
            var data = res.data;
            angular.forEach(data, function(incident) {
              var activityId = incident.activityId;
              var bpmnElement = bpmnElements[activityId];
              incident.activityName =
                (bpmnElement && (bpmnElement.name || bpmnElement.id)) ||
                activityId;

              var failedActivityId = incident.failedActivityId;
              var failedElement = bpmnElements[failedActivityId];
              incident.failedActivityName =
                (failedElement && (failedElement.name || failedElement.id)) ||
                failedActivityId;

              incident.linkable =
                bpmnElements[activityId] &&
                activityIdToInstancesMap[activityId] &&
                activityIdToInstancesMap[activityId].length > 0;
            });

            scope.incidents = data;
            scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

            var phase = scope.$root.$$phase;
            if (phase !== '$apply' && phase !== '$digest') {
              scope.$apply();
            }
          })
          .catch(angular.noop);
      }

      scope.getIncidentType = function(incident) {
        if (incident.incidentType === 'failedJob') {
          return 'Failed Job';
        }

        return incident.incidentType;
      };

      scope.getJobStacktraceUrl = function(incident) {
        var basePath = 'engine://engine/:engine';
        var id = incident.rootCauseIncidentConfiguration;
        var resource = 'job';
        var stacktraceName = 'stacktrace';

        if (incident.incidentType === 'failedExternalTask') {
          resource = 'external-task';
          stacktraceName = 'errorDetails';
        }

        if (scope.incidentsContext === 'history') {
          basePath = 'engine://engine/:engine/history';
          id = incident.historyConfiguration;
          resource += '-log';
          if (incident.incidentType === 'failedExternalTask') {
            stacktraceName = 'error-details';
          }
        }

        return Uri.appUri(`${basePath}/${resource}/${id}/${stacktraceName}`);
      };

      scope.incidentHasActions = function(incident) {
        return (
          scope.incidentsContext !== 'history' ||
          (scope.incidentsContext === 'history' &&
            incident.incidentType === 'failedJob' &&
            !incident.deleted &&
            !incident.resolved)
        );
      };
      scope.incidentVars = {read: ['incident', 'processData', 'filter']};
      scope.incidentActions = Views.getProviders({
        component: 'cockpit.incident.action'
      });

      function saveLocal(sortObj) {
        localConf.set(scope.localConfKey, sortObj);
      }

      function loadLocal(defaultValue) {
        return localConf.get(scope.localConfKey, defaultValue);
      }

      scope.viewVariable = function(incident) {
        $location.search(
          'incidentStacktrace',
          incident.rootCauseIncidentConfiguration
        );

        $http({
          method: 'GET',
          url: scope.getJobStacktraceUrl(incident),
          transformResponse: []
        }).then(function(res) {
          $modal
            .open({
              template: inspectTemplate,
              controller: [
                '$scope',
                'variable',
                function($scope, variable) {
                  $scope.variable = variable;
                  $scope.readonly = true;
                }
              ],
              size: 'lg',
              resolve: {
                variable: function() {
                  return {
                    value: res.data,
                    url: scope.getJobStacktraceUrl(incident)
                  };
                }
              }
            })
            .result.catch(angular.noop)
            .finally(function() {
              $location.search('incidentStacktrace', null);
            });
        });
      };

      const incidentResource = camAPI.resource('incident');
      scope.getAnnotationHandler = function(incident) {
        return function(annotation) {
          return incidentResource.setAnnotation({
            id: incident.id,
            annotation
          });
        };
      };

      if ($location.search().incidentStacktrace) {
        scope.viewVariable({
          rootCauseIncidentConfiguration: $location.search().incidentStacktrace
        });
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
  }
];

module.exports = Directive;
