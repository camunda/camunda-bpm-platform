'use strict';

var fs = require('fs');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/process-instance-table.html', 'utf8');
var searchConfig = JSON.parse(fs.readFileSync(__dirname + '/process-instance-search-config.json', 'utf8'));

module.exports = [ 'ViewsProvider', function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
    id: 'process-instances-table',
    label: 'Process Instances',
    template: template,
    controller: [
      '$scope', '$location', 'search', 'routeUtil', 'PluginProcessInstanceResource',
      function($scope,   $location,   search,   routeUtil,   PluginProcessInstanceResource) {
        var processDefinition = $scope.processDefinition;

        $scope.searchConfig = angular.copy(searchConfig);
        $scope.onSearchChange = updateView;

        function updateView(query, pages) {
          var page = pages.current,
              count = pages.size,
              firstResult = (page - 1) * count;

          var defaultParams = {
            processDefinitionId: processDefinition.id
          };

          var pagingParams = {
            firstResult: firstResult,
            maxResults: count,
            sortBy: 'startTime',
            sortOrder: 'desc'
          };

          var params = angular.extend({}, query, pagingParams, defaultParams);
          var countParams = angular.extend({}, query, defaultParams);

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
