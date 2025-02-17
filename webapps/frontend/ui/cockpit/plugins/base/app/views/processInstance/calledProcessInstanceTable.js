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

var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');
var angular = require('angular');

var template = require('./called-process-instance-table.html?raw');

module.exports = function(ngModule) {
  ngModule.controller('CalledProcessInstanceController', [
    '$scope',
    'PluginProcessInstanceResource',
    '$translate',
    'localConf',
    function($scope, PluginProcessInstanceResource, $translate, localConf) {
      // input: processInstance, processData

      var calledProcessInstanceData = $scope.processData.newChild($scope);

      // var processInstance = $scope.processInstance;

      // prettier-ignore
      $scope.headColumns = [
        { class: 'state', request: 'incidents', sortable: true, content: 'State' },
        { class: 'called-process-instance', request: 'id', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_PROCESS_INSTANCE')},
        { class: 'process-definition', request: 'processDefinitionLabel', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_PROCESS_DEFINITION')},
        { class: 'activity', request: 'instance.name', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_ACTIVITY')},
        { class: 'business-key', request: 'businessKey', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_BUSINESS_KEY')}
      ];

      // Default sorting
      $scope.sortObj = loadLocal({
        sortBy: 'processDefinitionLabel',
        sortOrder: 'asc',
        sortReverse: false
      });

      $scope.onSortChange = function(sortObj) {
        sortObj = sortObj || $scope.sortObj;
        // sortReverse required by anqular-sorting;
        sortObj.sortReverse = sortObj.sortOrder !== 'asc';
        saveLocal(sortObj);
        $scope.sortObj = sortObj;
      };

      function saveLocal(sortObj) {
        localConf.set('sortCalledProcessInstTab', sortObj);
      }
      function loadLocal(defaultValue) {
        return localConf.get('sortCalledProcessInstTab', defaultValue);
      }

      var filter = null;

      $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(
        null,
        'activityInstanceIdIn'
      );

      calledProcessInstanceData.observe(
        ['filter', 'instanceIdToInstanceMap'],
        function(newFilter, instanceIdToInstanceMap) {
          updateView(newFilter, instanceIdToInstanceMap);
        }
      );

      function updateView(newFilter, instanceIdToInstanceMap) {
        filter = angular.copy(newFilter);

        delete filter.page;
        delete filter.activityIds;
        delete filter.scrollToBpmnElement;

        // fix missmatch -> activityInstanceIds -> activityInstanceIdIn
        filter.activityInstanceIdIn = filter.activityInstanceIds;
        delete filter.activityInstanceIds;

        $scope.calledProcessInstances = null;

        $scope.loadingState = 'LOADING';
        PluginProcessInstanceResource.processInstances(
          {
            id: $scope.processInstance.id
          },
          filter
        )
          .$promise.then(function(response) {
            // angular.forEach(response.data, function (calledInstance) {
            angular.forEach(response, function(calledInstance) {
              var instance =
                instanceIdToInstanceMap[calledInstance.callActivityInstanceId];
              calledInstance.instance = instance;
              calledInstance.processDefinitionLabel =
                calledInstance.processDefinitionName ||
                calledInstance.processDefinitionKey;
            });

            $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
            $scope.calledProcessInstances = response;
          })
          .catch(angular.noop);
      }
    }
  ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
      id: 'called-process-instances-tab',
      label: 'PLUGIN_CALLED_PROCESS_INSTANCE_LABEL',
      template: template,
      controller: 'CalledProcessInstanceController',
      priority: 10
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  ngModule.config(Configuration);
};
