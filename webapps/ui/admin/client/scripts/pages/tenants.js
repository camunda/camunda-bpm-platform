'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tenants.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

var Controller = ['$scope', '$location', 'search', 'camAPI', 'page', function($scope, $location, search, camAPI, pageService) {

  var TenantResource = camAPI.resource('tenant');
    
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
    TenantResource.list(pagingParams, function(err, res) {
      $scope.tenantList = res;
      $scope.loadingState = res.length ? 'LOADED' : 'EMPTY';
    });

    TenantResource.count(function(err, res) {
      pages.total = res.count;
    });

  }

  TenantResource.options(function(err, res) {
    angular.forEach(res.links, function(link) {
      $scope.availableOperations[link.rel] = true;
    });
  });

  $scope.$root.showBreadcrumbs = true;

  pageService.titleSet('Tenants');

  pageService.breadcrumbsClear();

  pageService.breadcrumbsAdd({
    label: 'Tenants',
    href: '#/tenants/'
  });
}];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/tenants', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];
