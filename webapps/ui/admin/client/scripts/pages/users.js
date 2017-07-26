'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/users.html', 'utf8');
var searchConfig = JSON.parse(fs.readFileSync(__dirname + '/users-search-plugin-config.json', 'utf8'));

var angular = require('camunda-commons-ui/vendor/angular');

var Controller = ['$scope', '$location', 'search', 'UserResource', 'page', function($scope, $location, search, UserResource, pageService) {
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

    $scope.sortBy = $scope.sortBy || 'userId';
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

    $scope.userList = null;
    $scope.loadingState = 'LOADING';

    return UserResource.count(angular.extend({}, $scope.query)).$promise.then(function(data) {
      var total = data.count;

      return UserResource.query(angular.extend({}, $scope.query, queryParams)).$promise.then(function(data) {
        $scope.userList = data;
        $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

        return total;
      });
    });
  }

  $scope.availableOperations = {};
  UserResource.OPTIONS().$promise.then(function(response) {
    angular.forEach(response.links, function(link) {
      $scope.availableOperations[link.rel] = true;
    });
  });

  $scope.$root.showBreadcrumbs = true;

  pageService.titleSet('Users');

  pageService.breadcrumbsClear();

  pageService.breadcrumbsAdd({
    label: 'Users',
    href: '#/users/'
  });
}];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/users', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];
