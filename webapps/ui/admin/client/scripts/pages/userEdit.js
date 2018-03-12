'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/userEdit.html', 'utf8');
var groupTemplate = fs.readFileSync(__dirname + '/create-group-membership.html', 'utf8');
var tenantTemplate = fs.readFileSync(__dirname + '/create-tenant-user-membership.html', 'utf8');
var confirmationTemplate = fs.readFileSync(__dirname + '/generic-confirmation.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/users/:userId', {
    template: template,
    controller: [
      '$scope', 'page', '$routeParams', 'camAPI', 'Notifications', '$location', '$modal', 'authentication', 'unescape', '$translate',
      function($scope,   page,   $routeParams,   camAPI,   Notifications,   $location,   $modal,   authentication, unescape, $translate) {

        var AuthorizationResource = camAPI.resource('authorization'),
            GroupResource         = camAPI.resource('group'),
            TenantResource        = camAPI.resource('tenant'),
            UserResource          = camAPI.resource('user');

        var refreshBreadcrumbs = function() {
          page.breadcrumbsClear();
          page.breadcrumbsAdd({
            label: $translate.instant('USERS_USERS'),
            href: '#/users/'
          });
        };

        $scope.decodedUserId = unescape(encodeURIComponent($routeParams.userId));

        $scope.authenticatedUser = authentication;

        // used to display information about the user
        $scope.profile = null;

        // data model for the profile form (profileCopy is used for dirty checking)
        $scope.profile = null;
        $scope.profileCopy = null;

        // data model for the changePassword form
        $scope.credentials = {
          authenticatedUserPassword: '',
          password : '',
          password2 : ''
        };

        // list of the user's groups
        $scope.groupList = null;
        $scope.groupIdList = null;

        $scope.availableOperations = {};

        // common form validation //////////////////////////

        /** form must be valid & user must have made some changes */
        $scope.canSubmit = function(form, modelObject) {
          return form.$valid &&
            !form.$pristine &&
            // TODO: investigate "==" or "==="
            (modelObject == null || !angular.equals($scope[modelObject], $scope[modelObject+'Copy']));
        };

        // load options ////////////////////////////////////

        UserResource.options({ id : $scope.decodedUserId }, function(err, res) {
          angular.forEach(res.links, function(link) {
            $scope.availableOperations[link.rel] = true;
          });
        });

        // update profile form /////////////////////////////

        var loadProfile = $scope.loadProfile = function() {
          UserResource.profile({ id : $scope.decodedUserId }, function(err, res) {
            $scope.user = res;

            $scope.profile = angular.copy(res);
            $scope.profileCopy = angular.copy(res);

            page.titleSet($translate.instant('USERS_EDIT_USER', { user: $scope.user }));

            refreshBreadcrumbs();

            page.breadcrumbsAdd({
              label: [$scope.user.firstName, $scope.user.lastName].filter(function(v) { return !!v; }).join(' '),
              href: '#/users/' + $scope.user.id
            });
          });
        };

        $scope.updateProfile = function() {
          var resourceData = angular.extend({}, { id : $scope.decodedUserId }, $scope.profile);
          UserResource.updateProfile(resourceData, function(err) {
            if( err === null ) {
              Notifications.addMessage({
                type : 'success',
                status : $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
                message : $translate.instant('USERS_EDIT_SUCCESS_MSN')
              });
              loadProfile();

            } else {

              Notifications.addError({
                status : $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                message : $translate.instant('USERS_EDIT_FAILED')
              });
            }
          });
        };
        // update password form ////////////////////////////

        var resetCredentials = function(form) {
          $scope.credentials.authenticatedUserPassword = '';
          $scope.credentials.password = '';
          $scope.credentials.password2 = '';

          form.$setPristine();
        };

        $scope.updateCredentials = function(form) {
          var credentialsData = {
            authenticatedUserPassword: $scope.credentials.authenticatedUserPassword,
            password: $scope.credentials.password
          };

          var resourceData = angular.extend({}, { id : $scope.decodedUserId }, credentialsData);

          UserResource.updateCredentials(resourceData, function(err) {
            if( err === null ) {
              Notifications.addMessage({
                type: 'success',
                status: $translate.instant('NOTIFICATIONS_STATUS_PASSWORD'),
                message: $translate.instant('USERS_PASSWORD_CHANGED'),
                duration: 5000,
                exclusive: true
              });
              resetCredentials(form);

            } else {
              if( err.status === 400 ) {
                if( $scope.decodedUserId === $scope.authenticatedUser ) {
                  Notifications.addError({
                    status : $translate.instant('NOTIFICATIONS_STATUS_PASSWORD'),
                    message : $translate.instant('USERS_OLD_PASSWORD_NOT_VALID'),
                    exclusive : true
                  });

                } else {
                  Notifications.addError({
                    status : $translate.instant('NOTIFICATIONS_STATUS_PASSWORD'),
                    message : $translate.instant('USERS_PASSWORD_NOT_VALID'),
                    exclusive : true
                  });
                }
              } else {
                Notifications.addError({
                  status : $translate.instant('NOTIFICATIONS_STATUS_PASSWORD'),
                  message : $translate.instant('USERS_PASSWORD_COULD_NOT_CHANGE')
                });
              }
            }
          });
        };

        // delete user form /////////////////////////////

        $scope.deleteUser = function() {
          $modal.open({
            template: confirmationTemplate,
            controller: ['$scope', function($dialogScope) {
              $dialogScope.question = $translate.instant('USERS_USER_DELETE_CONFIRM', { user: $scope.user.id});
            }]
          }).result.then(function() {
            UserResource.delete({ id: $scope.decodedUserId }, function() {
              Notifications.addMessage({
                type: 'success',
                status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
                message: $translate.instant('USERS_USER_DELETE_SUCCESS', { user: $scope.user.id })
              });
              $location.path('/users');
            }
            );
          });
        };

        // Unlock User
        $scope.unlockUser = function() {
          UserResource.unlock({ id: $scope.decodedUserId })
            .then(function() {
              Notifications.addMessage({
                type: 'success',
                status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
                message: $translate.instant('USERS_USER_UNLOCK_SUCCESS', { user: $scope.user.id })
              });
              $location.path('/users');
            })
            .catch(function(e) {
              Notifications.addError({
                status : $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                message : $translate.instant(e.message)
              });
            });
        };

        // group form /////////////////////////////

        var groupsSorting = $scope.groupsSorting = null;

        $scope.onGroupsSortingChanged = function(_sorting) {
          groupsSorting = $scope.groupsSorting = $scope.groupsSorting || {};
          groupsSorting.sortBy = _sorting.sortBy;
          groupsSorting.sortOrder = _sorting.sortOrder;
          groupsSorting.sortReverse = _sorting.sortOrder !== 'asc';
        };

        $scope.$watch(function() {
          return $location.search().tab === 'groups' && groupsSorting;
        }, function(newValue) {
          return newValue && loadGroups();
        });

        var loadGroups = $scope.loadGroups = function() {
          $scope.groupLoadingState = 'LOADING';
          GroupResource.list({ member: $scope.decodedUserId }, function(err, res) {
            $scope.groupLoadingState = res.length ? 'LOADED' : 'EMPTY';

            $scope.groupList = res;
            $scope.groupIdList = [];
            angular.forEach($scope.groupList, function(group) {
              $scope.groupIdList.push(group.id);
            });
          });
        };

        $scope.removeGroup = function(groupId) {
          GroupResource.deleteMember({ userId: $scope.decodedUserId, id: groupId}, function() {
            Notifications.addMessage({
              type:'success',
              status:$translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
              message: $translate.instant('USERS_USER_DELETE_FROM_GROUP', { user: $scope.user.id })
            });
            loadGroups();
          }
          );
        };

        $scope.openCreateGroupMembershipDialog = function() {
          var dialogConfig = {
            ctrl: 'GroupMembershipDialogController',
            template: groupTemplate,
            callback: $scope.loadGroups,
            resolve: prepareResolveObject({
              idList : function() {
                return $scope.groupIdList;
              }
            })
          };

          openCreateDialog(dialogConfig);
        };

        var checkRemoveGroupMembershipAuthorized = function() {
          checkDeleteAuthorized('group membership', function(err, res) {
            $scope.availableOperations.removeGroup = res.authorized;
          });
        };


        // Tenant Form ///////////////////////////////////////////

        var tenantsSorting = $scope.tenantsSorting = null;

        $scope.onTenantsSortingChanged = function(_sorting) {
          tenantsSorting = $scope.tenantsSorting = $scope.tenantsSorting || {};
          tenantsSorting.sortBy = _sorting.sortBy;
          tenantsSorting.sortOrder = _sorting.sortOrder;
          tenantsSorting.sortReverse = _sorting.sortOrder !== 'asc';
        };

        $scope.$watch(function() {
          return $location.search().tab === 'tenants' && tenantsSorting;
        }, function(newValue) {
          return newValue && loadTenants();
        });

        var loadTenants = $scope.loadTenants = function() {
          $scope.tenantLoadingState = 'LOADING';
          TenantResource.list({ userMember: $scope.decodedUserId }, function(err, res) {
            $scope.tenantLoadingState = res.length ? 'LOADED' : 'EMPTY';

            $scope.tenantList = res;
            $scope.idList = [];
            angular.forEach($scope.tenantList, function(tenant) {
              $scope.idList.push(tenant.id);
            });
          });
        };

        $scope.removeTenant = function(tenantId) {
          TenantResource.deleteUserMember({userId: $scope.decodedUserId, id: tenantId}, function() {
            Notifications.addMessage({
              type:'success',
              status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
              message: $translate.instant('USERS_USER_DELETE_FROM_TENANT', { user: $scope.user.id })
            });
            loadTenants();
          }
          );
        };

        $scope.openCreateTenantMembershipDialog = function() {
          var dialogConfig = {
            ctrl: 'TenantMembershipDialogController',
            template: tenantTemplate,
            callback: $scope.loadTenants,
            resolve: prepareResolveObject({
              idList: function() {
                return $scope.idList;
              }
            })
          };

          openCreateDialog(dialogConfig);
        };

        var checkRemoveTenantMembershipAuthorized = function() {
          checkDeleteAuthorized('tenant membership', function(err, res) {
            $scope.availableOperations.removeTenant = res.authorized;
          });
        };


        // Modal Dialog Configuration ///////////////////////////////

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
                return $scope.user;
              },
              memberId : function() {
                return $scope.decodedUserId;
              }
            },

            listObj
          );
        };


        // Delete Authorization Check /////////////////////////

        var checkDeleteAuthorized = function(resourceName, cb) {
          AuthorizationResource.check({
            permissionName: 'DELETE',
            resourceName: resourceName,
            resourceType: 3
          }, cb);
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


        $scope.$root.showBreadcrumbs = true;

        page.titleSet($translate.instant('USERS_EDIT_USER'));
        refreshBreadcrumbs();

        loadProfile();
        checkRemoveGroupMembershipAuthorized();
        checkRemoveTenantMembershipAuthorized();

        if(!$location.search().tab) {
          $location.search({'tab': 'profile'});
          $location.replace();
        }
      }],
    authentication: 'required',
    reloadOnSearch: false
  });
}];
