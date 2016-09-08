'use strict';

var fs = require('fs');
var angular = require('angular');
var createSearchQueryForSearchWidget = require('../../../../../../common/scripts/util/create-search-query-for-search-widget');

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
        var processData = $scope.processData.newChild($scope);
        var processDefinition = $scope.processDefinition;
        var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };
        var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

        $scope.searchConfig = angular.copy(searchConfig);

        processData.provide('searches', $scope.searchConfig.searches);

        $scope.$watch('pages.current', function(newValue, oldValue) {
          if (newValue == oldValue) {
            return;
          }

          search('page', !newValue || newValue == 1 ? null : newValue);
        });

        $scope.$watch('searchConfig.searches', function(newValue, oldValue) {
          if (newValue !== oldValue) {
            processData.set('searches', $scope.searchConfig.searches);
          }
        }, true);

        processData.observe('filter', function(filter) {
          var selectedActivityIds = filter.activityIds;

          if (selectedActivityIds !== null) {
            var urlParams = search();
            var searches = JSON.parse(urlParams.searchQuery || '[]');

            searches = removeActivitySearches(searches)
              .concat(createSearchesForActivityIds(selectedActivityIds));

            search(angular.extend(urlParams, {
              searchQuery: JSON.stringify(searches)
            }));
          }
        });

        processData.observe('searches', updateView);

        processData.observe(['searches'], function(searches) {
          if (searches) {
            $scope.filter.activityIds = searches
              .filter(function(search) {
                return search.type.value.key === 'activityIdIn';
              })
              .map(function(search) {
                return search.value.value;
              });
          }
        });

        function updateView(searches) {
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

          var query = createSearchQueryForSearchWidget(searches, ['activityIdIn']);
          var params = angular.extend({}, query, pagingParams, defaultParams);

          $scope.processInstances = null;
          $scope.loadingState = 'LOADING';

          PluginProcessInstanceResource.query(pagingParams, params).$promise.then(function(data) {
            $scope.processInstances = data;
            $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';
          });

          PluginProcessInstanceResource.count(query).$promise.then(function(data) {
            pages.total = data.count;
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

function removeActivitySearches(searches) {
  return searches.filter(function(search) {
    return search.type !== 'activityIdIn';
  });
}

function createSearchesForActivityIds(activityIds) {
  return activityIds.map(createActivitySearch);
}

function createActivitySearch(value) {
  return {
    type: 'activityIdIn',
    operator: 'eq',
    value: value
  };
}
