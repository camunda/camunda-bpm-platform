'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/groups.html', 'utf8');
var searchConfig = JSON.parse(fs.readFileSync(__dirname + '/groups-search-plugin-config.json', 'utf8'));

var angular = require('camunda-commons-ui/vendor/angular');

var Controller = ['$scope', 'page', '$location', 'search', 'GroupResource', function($scope, pageService, $location, search, GroupResource) {
  $scope.searchConfig = angular.copy(searchConfig);
  $scope.onSearchChange = updateView;

  $scope.query = $scope.pages = $scope.sortBy = $scope.sortOrder = null;

  $scope.orderClass = function(forColumn) {
    forColumn = forColumn || $scope.sortBy;
    return 'glyphicon-' + ({
      none: 'minus',
      desc: 'chevron-down',
      asc:  'chevron-up'
    }[forColumn === $scope.sortBy ? $scope.sortOrder : 'none']);
  };

  $scope.changeOrder = function(column) {
    $scope.sortBy = column;
    $scope.sortOrder = $scope.sortOrder === 'desc' ? 'asc' : 'desc';

    updateView();
  };

  function updateView(query, pages) {
    if (query && pages) {
      $scope.query = query;
      $scope.pages = pages;
    }

    $scope.sortBy = $scope.sortBy || 'id';
    $scope.sortOrder = $scope.sortOrder || 'asc';

    var page = $scope.pages.current,
        count = $scope.pages.size,
        firstResult = (page - 1) * count;

    var queryParams = {
      firstResult: firstResult,
      maxResults: count,
      sortBy: $scope.sortBy,
      sortOrder: $scope.sortOrder
    };

    $scope.groupList = null;
    $scope.loadingState = 'LOADING';

    return GroupResource.count(Object.assign({}, $scope.query)).$promise.then(function(data) {
      var total = data.count;

      return GroupResource.query(Object.assign({}, $scope.query, queryParams)).$promise.then(function(data) {
        $scope.groupList = data;
        $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

        return total;
      });
    });
  }

  $scope.availableOperations = {};
  GroupResource.OPTIONS().$promise.then(function(response) {
    angular.forEach(response.links, function(link) {
      $scope.availableOperations[link.rel] = true;
    });
  });

  $scope.$root.showBreadcrumbs = true;

  pageService.titleSet('Groups');

  pageService.breadcrumbsClear();

  pageService.breadcrumbsAdd({
    label: 'Groups',
    href: '#/groups'
  });
}];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/groups', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];
