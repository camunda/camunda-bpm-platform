'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/groups.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

var Controller = ['$scope', 'page', '$location', 'search', 'GroupResource', function($scope, pageService, $location, search, GroupResource) {

  $scope.$root.showBreadcrumbs = true;

  pageService.titleSet('Groups');

  pageService.breadcrumbsClear();

  pageService.breadcrumbsAdd([
    {
      label: 'Groups',
      href: '#/groups'
    }
  ]);

  $scope.availableOperations={};
  $scope.loadingState = 'LOADING';

  var pages = $scope.pages = { size: 25, total: 0 };

  $scope.$watch(function() {
    return parseInt(($location.search() || {}).page || '1');
  }, function(newValue) {
    pages.current = newValue;
    updateView();
  });

  $scope.pageChange = function(page) {
    search.updateSilently({ page: !page || page == 1 ? null : page });
  };

  function updateView() {
    var page = pages.current,
        count = pages.size,
        firstResult = (page - 1) * count;

    var pagingParams = {
      firstResult: firstResult,
      maxResults: count
    };

    $scope.loadingState = 'LOADING';
    GroupResource.query(pagingParams).$promise.then(function(response) {
      $scope.groupList = response;
      $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
    });

    GroupResource.count().$promise.then(function(response) {
      pages.total = response.count;
    });

  }

  GroupResource.OPTIONS().$promise.then(function(response) {
    angular.forEach(response.links, function(link) {
      $scope.availableOperations[link.rel] = true;
    });
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
