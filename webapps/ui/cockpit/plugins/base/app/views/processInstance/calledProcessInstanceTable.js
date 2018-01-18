'use strict';

var fs = require('fs');
var searchWidgetUtils = require('../../../../../../common/scripts/util/search-widget-utils');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/called-process-instance-table.html', 'utf8');

module.exports = function(ngModule) {
  ngModule.controller('CalledProcessInstanceController', [
    '$scope', 'PluginProcessInstanceResource', '$translate',  'localConf',
    function($scope,   PluginProcessInstanceResource, $translate, localConf) {

      // input: processInstance, processData

      var calledProcessInstanceData = $scope.processData.newChild($scope);

      // var processInstance = $scope.processInstance;

      $scope.headColumns = [
        { class: 'state', request: 'incidents', sortable: true, content: 'State' },
        { class: 'called-process-instance', request: 'id', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_PROCESS_INSTANCE')},
        { class: 'process-definition', request: 'processDefinitionLabel', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_PROCESS_DEFINITION')},
        { class: 'activity', request: 'instance', sortable: true, content: $translate.instant('PLUGIN_CALLED_PROCESS_ACTIVITY')}
      ];

      // Default sorting
      $scope.sortObj   = loadLocal({ sortBy: 'processDefinitionId', sortOrder: 'asc', sortReverse: false});

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

      $scope.getSearchQueryForSearchType = searchWidgetUtils.getSearchQueryForSearchType.bind(null, 'activityInstanceIdIn');

      calledProcessInstanceData.observe([ 'filter', 'instanceIdToInstanceMap' ], function(newFilter, instanceIdToInstanceMap) {
        updateView(newFilter, instanceIdToInstanceMap);
      });

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
        PluginProcessInstanceResource
          .processInstances({
            id: $scope.processInstance.id
          }, filter)
          .$promise.then(function(response) {

            // angular.forEach(response.data, function (calledInstance) {
            angular.forEach(response, function(calledInstance) {
              var instance = instanceIdToInstanceMap[calledInstance.callActivityInstanceId];
              calledInstance.instance = instance;
              calledInstance.processDefinitionLabel = calledInstance.processDefinitionName || calledInstance.processDefinitionKey;
            });

            $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
            $scope.calledProcessInstances = response;
          });
      }
    }]);

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
