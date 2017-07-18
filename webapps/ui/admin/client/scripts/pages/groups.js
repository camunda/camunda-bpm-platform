'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/groups.html', 'utf8');
var searchConfig = JSON.parse(fs.readFileSync(__dirname + '/groups-search-plugin-config.json', 'utf8'));

var angular = require('camunda-commons-ui/vendor/angular');

var Controller = ['$scope', 'page', '$location', 'search', 'GroupResource', function($scope, pageService, $location, search, GroupResource) {
  $scope.searchConfig = angular.copy(searchConfig);
  $scope.onSearchChange = updateView;

  function updateView(query, pages) {
    var page = pages.current,
        count = pages.size,
        firstResult = (page - 1) * count;

    var pagingParams = {
      firstResult: firstResult,
      maxResults: count
    };

    var params = angular.extend({}, query, pagingParams);

    $scope.groupList = null;
    $scope.loadingState = 'LOADING';

    return GroupResource.count(query).$promise.then(function(data) {
      var total = data.count;

      return GroupResource.query(params).$promise.then(function(data) {
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
