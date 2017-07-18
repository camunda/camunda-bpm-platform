'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tenants.html', 'utf8');
var searchConfig = JSON.parse(fs.readFileSync(__dirname + '/tenants-search-plugin-config.json', 'utf8'));

var angular = require('camunda-commons-ui/vendor/angular');

var Controller = ['$scope', '$location', 'search', 'TenantResource', 'camAPI', 'page', function($scope, $location, search, TenantResource, camAPI, pageService) {

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

    $scope.tenantList = null;
    $scope.loadingState = 'LOADING';

    return TenantResource.count(query).$promise.then(function(data) {
      var total = data.count;

      return TenantResource.query(params).$promise.then(function(data) {
        $scope.tenantList = data;
        $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

        return total;
      });
    });
  }

  $scope.availableOperations = {};
  camAPI.resource('tenant').options(function(err, res) {
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
