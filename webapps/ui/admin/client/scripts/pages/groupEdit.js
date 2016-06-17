'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/groupEdit.html', 'utf8');
var tenantTemplate = fs.readFileSync(__dirname + '/create-tenant-group-membership.html', 'utf8');
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
  function(
    $scope,
    pageService,
    $routeParams,
    search,
    camAPI,
    Notifications,
    $location,
    $modal
  ) {

    var AuthorizationResource = camAPI.resource('authorization'),
        GroupResource         = camAPI.resource('group'),
        TenantResource        = camAPI.resource('tenant'),
        UserResource          = camAPI.resource('user');


    var refreshBreadcrumbs = function() {
      pageService.breadcrumbsClear();
      pageService.breadcrumbsAdd({
        label: 'Groups',
        href: '#/groups/'
      });
    };

    var encodeId = function(id) {
      return id
        .replace(/\//g, '%2F')
        .replace(/\\/g, '%5C');
    };

    $scope.$root.showBreadcrumbs = true;

    pageService.titleSet('Group');
    refreshBreadcrumbs();

    $scope.group = null;
    $scope.groupName = null;
    $scope.encodedGroupId = encodeId($routeParams.groupId);

    $scope.availableOperations = {};
    $scope.groupUserList = null;
    $scope.tenantList = null;

    var groupUserPages = $scope.groupUserPages = { size: 25, total: 0 };
    var groupTenantPages = $scope.groupTenantPages = { size: 25, total: 0 };

    // common form validation //////////////////////////

    /** form must be valid & user must have made some changes */
    $scope.canSubmit = function(form, modelObject) {
      return form.$valid &&
             !form.$pristine &&
             (!modelObject || !angular.equals($scope[modelObject], $scope[modelObject + 'Copy']));
    };

    // update group form /////////////////////////////

    var loadGroup = $scope.loadGroup = function() {
      $scope.groupLoadingState = 'LOADING';
      GroupResource.get({ id : $scope.encodedGroupId }, function(err, res) {
        $scope.groupLoadingState = 'LOADED';
        $scope.group = res;
        $scope.groupName = (res.name ? res.name : res.id);
        $scope.groupCopy = angular.copy(res);

        pageService.titleSet($scope.groupName + ' Group');

        refreshBreadcrumbs();
        pageService.breadcrumbsAdd({
          label: $scope.groupName,
          href: '#/groups/' + $scope.group.id
        });

      }, function() {
        $scope.groupLoadingState = 'ERROR';
      });
    };

    $scope.$watch(function() {
      return $location.search().tab === 'users' && parseInt(($location.search() || {}).page || '1');
    }, function(newValue) {
      if (newValue) {
        groupUserPages.current = newValue;
        updateGroupUserView();
      }
    });

    $scope.$watch(function() {
      return $location.search().tab === 'tenants' && parseInt(($location.search() || {}).page || '1');
    }, function(newValue) {
      if( newValue ) {
        groupTenantPages.current = newValue;
        updateGroupTenantView();
      }
    });

    $scope.pageChange = function(page) {
      search.updateSilently({ page: !page || page == 1 ? null : page });
    };

    var preparePaging = function(pages) {
      var page = pages.current,
          count = pages.size,
          firstResult = (page - 1) * count;

      return {
        firstResult: firstResult,
        maxResults: count
      };
    };

    var updateGroupUserView = function() {
      var pagingParams = preparePaging(groupUserPages);
      var searchParams = { memberOfGroup : $scope.encodedGroupId };

      $scope.userLoadingState = 'LOADING';
      UserResource.list(angular.extend({}, searchParams, pagingParams), function(err, res) {
        if( err === null ) {
          $scope.groupUserList = res;
          $scope.userLoadingState = res.length ? 'LOADED' : 'EMPTY';
        } else {
          $scope.userLoadingState = 'ERROR';
        }
      });

      UserResource.count(searchParams, function(err, res) {
        groupUserPages.total = res.count;
      });
    };

    var updateGroupTenantView = function() {
      var pagingParams = preparePaging(groupTenantPages);
      var searchParams = { groupMember : $scope.encodedGroupId };

      $scope.tenantLoadingState = 'LOADING';
      TenantResource.list(angular.extend({}, searchParams, pagingParams), function(err, res) {
        if( err === null ) {
          $scope.tenantList = res;

          $scope.idList = [];
          angular.forEach($scope.tenantList, function(tenant) {
            $scope.idList.push(tenant.id);
          });

          $scope.tenantLoadingState = res.length ? 'LOADED' : 'EMPTY';
        } else {
          $scope.tenantLoadingState = 'ERROR';
        }
      });

      TenantResource.count(searchParams, function(err, res) {
        groupTenantPages.total = res.count;
      });

      checkRemoveTenantMembershipAuthorized();
    };

    var checkRemoveTenantMembershipAuthorized = function() {
      AuthorizationResource.check({
        permissionName: 'DELETE',
        resourceName: 'tenant membership',
        resourceType: 3
      }, function(err, res) {
        $scope.availableOperations.removeTenant = res.authorized;
      });
    };

    GroupResource.options({ id: $scope.encodedGroupId }, function(err, res) {
      angular.forEach(res.links, function(link) {
        $scope.availableOperations[link.rel] = true;
      });
    });

    $scope.removeTenant = function(tenantId) {
      var encodedTenantId = encodeId(tenantId);

      TenantResource.deleteGroupMember({ groupId: $scope.encodedGroupId, id: encodedTenantId }, function() {
        Notifications.addMessage({
          type:'success',
          status:'Success',
          message:'Group '+$scope.group.id+' removed from tenant.'
        });
        updateGroupTenantView();
      });
    };

    $scope.updateGroup = function() {
      GroupResource.update(angular.extend({}, { groupId: $scope.encodedGroupId }, $scope.group), function(err) {
        if( err === null ) {
          Notifications.addMessage({
            type : 'success',
            status : 'Success',
            message : 'Group successfully updated.'
          });
          loadGroup();

        } else {
          Notifications.addError({
            status : 'Failed',
            message : 'Failed to update group'
          });
        }
      });
    };

    // delete group form /////////////////////////////

    $scope.deleteGroup = function() {
      $modal.open({
        template: confirmationTemplate,
        controller: ['$scope', function($dialogScope) {
          $dialogScope.question = 'Really delete group ' + $scope.group.id + '?';
        }]
      }).result.then(function() {
        GroupResource.delete({ id: $scope.encodedGroupId }, function(err) {
          if( err === null ) {
            Notifications.addMessage({
              type : 'success',
              status : 'Success',
              message : 'Group ' + $scope.group.id + ' successfully deleted.'
            });
            $location.path('/groups');
          } else {
            Notifications.addError({
              status : 'Failed',
              message : 'Failed to delete group'
            });
          }
        });
      });
    };

    // tenant membership dialog /////////////////////////
    var openCreateDialog = function(dialogCfg) {
      var dialog = $modal.open({
        controller: dialogCfg.ctrl,
        template: dialogCfg.template,
        resolve: dialogCfg.resolve
      });

      dialog.result.then(function(result) {

        if (result == 'SUCCESS') {
          dialogCfg.callback();
        }
      });
    };

    var prepareResolveObject = function(listObj) {
      return angular.extend(
        {},

        {
          member : function() {
            return $scope.group;
          },
          memberId : function() {
            return $scope.encodedGroupId;
          }
        },

        listObj
      );
    };

    $scope.openCreateTenantMembershipDialog = function() {
      var dialogConfig = {
        ctrl: 'TenantMembershipDialogController',
        template: tenantTemplate,
        callback: updateGroupTenantView,
        resolve: prepareResolveObject({
          idList: function() {
            return $scope.idList;
          }
        })
      };

      openCreateDialog(dialogConfig);
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

    loadGroup();

    if(!$location.search().tab) {
      $location.search({'tab': 'group'});
      $location.replace();
    }

  }];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/groups/:groupId', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];
