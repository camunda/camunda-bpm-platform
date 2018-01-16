'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/groups.html', 'utf8');
var searchConfig = JSON.parse(fs.readFileSync(__dirname + '/groups-search-plugin-config.json', 'utf8'));

var angular = require('camunda-commons-ui/vendor/angular');

var Controller = ['$scope', 'page', '$location', 'search', 'GroupResource', '$translate', function($scope, pageService, $location, search, GroupResource, $translate) {
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

  $scope.blocked = true;
  $scope.onSearchChange = updateView;

  $scope.query = $scope.pages = null;
  var sorting;

  $scope.onSortInitialized = function(_sorting) {
    sorting = _sorting;
    $scope.blocked = false;
  };

  $scope.onSortChanged = function(_sorting) {
    sorting = _sorting;
    updateView();
  };

  function updateView(query, pages) {
    if (query && pages) {
      $scope.query = query;
      $scope.pages = pages;
    }

    var page = $scope.pages.current,
        count = $scope.pages.size,
        firstResult = (page - 1) * count;

    var queryParams = {
      firstResult: firstResult,
      maxResults: count,
      sortBy: sorting.sortBy,
      sortOrder: sorting.sortOrder
    };

    $scope.groupList = null;
    $scope.loadingState = 'LOADING';

    return GroupResource.count(angular.extend({}, $scope.query)).$promise.then(function(data) {
      var total = data.count;

      return GroupResource.query(angular.extend({}, $scope.query, queryParams)).$promise.then(function(data) {
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

  pageService.titleSet($translate.instant('GROUPS_GROUP'));

  pageService.breadcrumbsClear();

  pageService.breadcrumbsAdd({
    label: $translate.instant('GROUPS_GROUP'),
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
