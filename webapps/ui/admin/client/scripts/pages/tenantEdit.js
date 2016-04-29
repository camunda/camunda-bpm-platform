'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tenantEdit.html', 'utf8');
var confirmationTemplate = fs.readFileSync(__dirname + '/generic-confirmation.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

  var Controller = [
    '$scope',
    'page',
    '$routeParams',
    'search',
    'TenantResource',
    'GroupResource',
    'UserResource',
    'AuthorizationResource',
    'Notifications',
    '$location',
    '$modal',
  function (
    $scope,
    pageService,
    $routeParams,
    search,
    TenantResource,
    GroupResource,
    UserResource,
    AuthorizationResource,
    Notifications,
    $location,
    $modal
  ) {

    $scope.$root.showBreadcrumbs = true;

    pageService.titleSet('Tenant');

    pageService.breadcrumbsClear();

    pageService.breadcrumbsAdd([
      {
        label: 'Tenants',
        href: '#/tenants'
      }
    ]);

    $scope.tenant = null;
    $scope.tenantName = null;
    $scope.encodedTenantId = $routeParams.tenantId
                                            .replace(/\//g, '%2F')
                                            .replace(/\\/g, '%5C');

    $scope.availableOperations = {};
    $scope.tenantGroupList = null;
    $scope.tenantUserList = null;

    var tenantGroupPages = $scope.tenantGroupPages = { size: 25, total: 0 },
        tenantUserPages = $scope.tenantUserPages = { size: 25, total: 0 };

    // common form validation //////////////////////////

    /** form must be valid & user must have made some changes */
    $scope.canSubmit = function(form, modelObject) {
      return form.$valid &&
             !form.$pristine &&
             (!modelObject || !angular.equals($scope[modelObject], $scope[modelObject + 'Copy']));
    };

    // update tenant form /////////////////////////////

    var loadTenant = $scope.loadTenant = function() {
      $scope.tenantLoadingState = 'LOADING';
      TenantResource.get({ tenantId : $scope.encodedTenantId }).$promise.then(function(response) {
        $scope.tenantLoadingState = 'LOADED';
        $scope.tenant = response;
        $scope.tenantName = (!!response.name ? response.name : response.id);
        $scope.tenantCopy = angular.copy(response);

        pageService.titleSet($scope.tenantName + ' Tenant');

        pageService.breadcrumbsAdd([
          {
            label: $scope.tenantName,
            href: '#/tenants/' + $scope.tenant.id
          }
        ]);

      }, function () {
        $scope.tenantLoadingState = 'ERROR';
      });
    };

    // update users list
    $scope.$watch(function() {
      return $location.search().tab === 'users' && parseInt(($location.search() || {}).page || '1');
    }, function(newValue) {
      if (newValue) {
        tenantUserPages.current = newValue;
        updateTenantUserView();
      }
    });

    // update groups list
    $scope.$watch(function() {
      return $location.search().tab === 'groups' && parseInt( ($location.search() || {}).page || '1' );
    }, function(newValue) {
      if(newValue) {
        tenantGroupPages.current = newValue;
        updateTenantGroupView();
      }
    });

    $scope.pageChange = function(page) {
      search.updateSilently({ page: !page || page == 1 ? null : page });
    };

    function prepareTenantMemberView(memberPages) {
      var page = memberPages.current,
          count = memberPages.size,
          firstResult = ( page - 1 ) * count;

      return {
        searchParams : {
          memberOfTenant : $scope.encodedTenantId
        },
        pagingParams : {
          firstResult : firstResult,
          maxResults : count
        }
      }
    }

    function updateTenantGroupView() {
      var prep = prepareTenantMemberView(tenantGroupPages);

      $scope.groupLoadingState = 'LOADING';
      GroupResource.query(angular.extend({}, prep.searchParams, prep.pagingParams)).$promise.then(function(response) {
        $scope.tenantUserList = response;
        $scope.groupLoadingState = response.length ? 'LOADED' : 'EMPTY';
      }, function() {
        $scope.groupLoadingState = 'ERROR';
      });

      GroupResource.count(prep.searchParams).$promise.then(function(response) {
        tenantGroupPages.total = response.count;
      });
    }

    function updateTenantUserView() {
      var prep = prepareTenantMemberView(tenantUserPages);

      $scope.userLoadingState = 'LOADING';
      UserResource.query(angular.extend({}, prep.searchParams, prep.pagingParams)).$promise.then(function(response) {
        $scope.tenantUserList = response;
        $scope.userLoadingState = response.length ? 'LOADED' : 'EMPTY';
      }, function() {
        $scope.userLoadingState = 'ERROR';
      });

      UserResource.count(prep.searchParams).$promise.then(function(response) {
        tenantUserPages.total = response.count;
      });
    }

    TenantResource.OPTIONS({tenantId : $scope.encodedTenantId}).$promise.then(function(response) {
      angular.forEach(response.links, function(link){
        $scope.availableOperations[link.rel] = true;
      });
    });

    $scope.updateTenant = function() {

      TenantResource.update({ tenantId: $scope.encodedTenantId }, $scope.tenant).$promise.then(function() {
          Notifications.addMessage({
            type: 'success',
            status: 'Success',
            message: 'Tenant successfully updated.'
          });
          loadTenant();
        },
        function() {
          Notifications.addError({
            status: 'Failed',
            message: 'Failed to update tenant'
          });
        }
      );
    };

    // delete group form /////////////////////////////

    $scope.deleteTenant = function() {
      $modal.open({
        template: confirmationTemplate,
        controller: ['$scope', function ($dialogScope) {
          $dialogScope.question = 'Really delete tenant ' + $scope.tenant.id + '?';
        }]
      }).result.then(function () {
        TenantResource.delete({
          'tenantId':$scope.encodedTenantId
        }).$promise.then(
          function() {
            Notifications.addMessage({
              type: 'success',
              status: 'Success',
              message: 'Tenant ' + $scope.tenant.id + ' successfully deleted.'
            });
            $location.path('/tenants');
          }
        );
      });
    };

    // page controls ////////////////////////////////////

    $scope.show = function(fragment) {
      return fragment == $location.search().tab;
    };

    $scope.activeClass = function(link) {
      var path = $location.absUrl();
      return path.indexOf(link) != -1 ? 'active' : '';
    };

    // initialization ///////////////////////////////////

    loadTenant();

    if(!$location.search().tab) {
      $location.search({'tab': 'tenant'});
      $location.replace();
    }

  }];

  module.exports = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/tenants/:tenantId', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }];
