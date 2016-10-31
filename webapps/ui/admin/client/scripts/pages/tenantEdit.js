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
  'camAPI',
  'Notifications',
  '$location',
  '$modal',
  'unescape',
  function(
    $scope,
    pageService,
    $routeParams,
    search,
    camAPI,
    Notifications,
    $location,
    $modal,
    unescape
  ) {

    var TenantResource        = camAPI.resource('tenant'),
        GroupResource         = camAPI.resource('group'),
        UserResource          = camAPI.resource('user');

    $scope.$root.showBreadcrumbs = true;



    pageService.titleSet('Tenant');



    function refreshBreadcrumbs() {
      pageService.breadcrumbsClear();

      pageService.breadcrumbsAdd([{
        label: 'Tenants',
        href: '#/tenants'
      }]);
    }
    refreshBreadcrumbs();


    $scope.tenant = null;
    $scope.tenantName = null;
    $scope.decodedTenantId = unescape(encodeURIComponent($routeParams.tenantId));
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

      TenantResource.get($scope.decodedTenantId, function(err, res) {
        $scope.tenantLoadingState = 'LOADED';
        $scope.tenant = res;
        $scope.tenantName = (res.name ? res.name : res.id);
        $scope.tenantCopy = angular.copy(res);

        pageService.titleSet($scope.tenantName + ' Tenant');

        refreshBreadcrumbs();

        pageService.breadcrumbsAdd([{
          label: $scope.tenantName,
          href: '#/tenants/' + $scope.tenant.id
        }]);

      }, function() {
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
          memberOfTenant : $scope.decodedTenantId
        },
        pagingParams : {
          firstResult : firstResult,
          maxResults : count
        }
      };
    }

    function updateTenantGroupView() {
      var prep = prepareTenantMemberView(tenantGroupPages);

      $scope.groupLoadingState = 'LOADING';
      GroupResource.list(angular.extend({}, prep.searchParams, prep.pagingParams), function(err, res) {
        if( err === null ) {
          $scope.tenantGroupList = res;
          $scope.groupLoadingState = res.length ? 'LOADED' : 'EMPTY';
        } else {
          $scope.groupLoadingState = 'ERROR';
        }
      });

      GroupResource.count(prep.searchParams, function(err, res) {
        tenantGroupPages.total = res.count;
      });
    }

    function updateTenantUserView() {
      var prep = prepareTenantMemberView(tenantUserPages);

      $scope.userLoadingState = 'LOADING';
      UserResource.list(angular.extend({}, prep.searchParams, prep.pagingParams), function(err, res) {
        if( err === null ) {
          $scope.tenantUserList = res;
          $scope.userLoadingState = res.length ? 'LOADED' : 'EMPTY';
        } else {
          $scope.userLoadingState = 'ERROR';
        }
      });

      UserResource.count(prep.searchParams, function(err, res) {
        tenantUserPages.total = res.count;
      });
    }

    TenantResource.options($scope.decodedTenantId, function(err, res) {
      angular.forEach(res.links, function(link) {
        $scope.availableOperations[link.rel] = true;
      });
    });

    $scope.updateTenant = function() {

      var updateData = {
        id: $scope.decodedTenantId,
        name: $scope.tenant.name
      };

      TenantResource.update(updateData, function(err) {
        if( err === null ) {
          Notifications.addMessage({
            type : 'success',
            status : 'Success',
            message : 'Tenant successfully updated.'
          });
          loadTenant();
        } else {
          Notifications.addError({
            status : 'Failed',
            message : 'Failed to update tenant.'
          });
        }
      });
    };

    // delete group form /////////////////////////////

    $scope.deleteTenant = function() {
      $modal.open({
        template: confirmationTemplate,
        controller: ['$scope', function($dialogScope) {
          $dialogScope.question = 'Really delete tenant ' + $scope.tenant.id + '?';
        }]
      }).result.then(function() {
        TenantResource.delete({ id: $scope.decodedTenantId }, function(err) {
          if(err === null) {
            Notifications.addMessage({
              type: 'success',
              status: 'Success',
              message: 'Tenant ' + $scope.tenant.id + ' successfully deleted.'
            });
            $location.path('/tenants');
          } else {
            Notifications.addMessage({
              type: 'success',
              status: 'Success',
              message: 'Failed to delete tenant ' + $scope.tenant.id + '.'
            });
          }
        });
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
