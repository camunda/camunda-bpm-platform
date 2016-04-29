'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tenants.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

  var Controller = ['$scope', '$location', 'search', 'TenantResource', 'page', function ($scope, $location, search, TenantResource, pageService) {

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
      TenantResource.query(pagingParams).$promise.then(function(response) {
        $scope.tenantList = response;
        $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
      });

      TenantResource.count().$promise.then(function(response) {
        pages.total = response.count;
      });

    }

    TenantResource.OPTIONS().$promise.then(function(response) {
      angular.forEach(response.links, function(link){
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
