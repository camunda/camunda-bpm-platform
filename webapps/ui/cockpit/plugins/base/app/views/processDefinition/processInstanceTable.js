'use strict';

var fs = require('fs');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/process-instance-table.html', 'utf8');
var searchConfig = JSON.parse(fs.readFileSync(__dirname + '/process-instance-search-config.json', 'utf8'));

module.exports = [ 'ViewsProvider', function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
    id: 'process-instances-table',
    label: 'PLUGIN_PROCESS_INSTANCES_LABEL',
    template: template,
    controller: [
      '$scope', '$location', 'search', 'routeUtil', 'PluginProcessInstanceResource', '$translate', 'localConf',
      function($scope,   $location,   search,   routeUtil,   PluginProcessInstanceResource, $translate, localConf) {

        var processDefinition = $scope.processDefinition;
        $scope.onSearchChange = updateView;
        $scope.onSortChange   = updateView;

        $scope.headColumns = [
          { class: 'state',        request: 'state',       sortable: false, content: 'PLUGIN_PROCESS_INSTANCE_STATE'},
          { class: 'instance-id',  request: 'instanceId',  sortable: false, content: 'PLUGIN_PROCESS_INSTANCE_ID'},
          { class: 'start-time',   request: 'startTime',   sortable: true,  content: 'PLUGIN_PROCESS_INSTANCE_START_TIME'},
          { class: 'business-key', request: 'businessKey', sortable: false, content: 'PLUGIN_PROCESS_INSTANCE_BUSINESS_KEY'}
        ];

        // Default sorting
        var defaultValue=  {sortBy: 'startTime', sortOrder: 'desc'};
        $scope.sortObj   = loadLocal(defaultValue);


        $scope.searchConfig = angular.copy(searchConfig);
        angular.forEach(searchConfig.tooltips, function(translation, tooltip) {
          $scope.searchConfig.tooltips[tooltip] = $translate.instant(translation);
        });
        $scope.searchConfig.types.map(function(type) {
          type.id.value = $translate.instant(type.id.value);
          if (type.operators) {
            type.operators = type.operators.map(function(op) {
              op.value = $translate.instant(op.value);
              return op;
            });
          }
          return type;
        });

        function updateView(query, pages, sortObj) {
          $scope.pagesObj = pages   || $scope.pagesObj ;
          $scope.queryObj = query   || $scope.queryObj;
          sortObj         = sortObj || $scope.sortObj;

          saveLocal(sortObj);

          var page        =  $scope.pagesObj.current,
              queryParams =  $scope.queryObj,
              count       =  $scope.pagesObj.size,
              firstResult = (page - 1) * count;

          var defaultParams = {
            processDefinitionId: processDefinition.id
          };

          var pagingParams = {
            firstResult    : firstResult,
            maxResults     : count,
            sortBy         : sortObj.sortBy,
            sortOrder      : sortObj.sortOrder
          };

          var params = angular.extend({}, query, pagingParams, defaultParams);
          var countParams = angular.extend({}, queryParams, defaultParams);

          $scope.processInstances = null;
          $scope.loadingState = 'LOADING';

          return PluginProcessInstanceResource.count(countParams).$promise.then(function(data) {
            var total = data.count;

            return PluginProcessInstanceResource.query(pagingParams, params).$promise.then(function(data) {
              $scope.processInstances = data;
              $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';
              return total;
            });
          });
        }

        function saveLocal(sortObj) {
          localConf.set('sortProcInst', sortObj);

        }

        function loadLocal(defaultValue) {
          return localConf.get('sortProcInst', defaultValue);
        }

        $scope.getProcessInstanceUrl = function(processInstance, params) {
          var path = '#/process-instance/' + processInstance.id;
          var searches = angular.extend({}, ($location.search() || {}), (params || {}));

          var keepSearchParams = [ 'viewbox' ];
          for (var i in params) {
            keepSearchParams.push(i);
          }

          return routeUtil.redirectTo(path, searches, keepSearchParams);
        };
      }],
    priority: 10
  });
}];
