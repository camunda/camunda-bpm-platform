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
      '$scope', 'page', '$routeParams', 'camAPI', 'Notifications', '$location', '$modal', 'authentication',
      function($scope,   page,   $routeParams,   camAPI,   Notifications,   $location,   $modal,   authentication) {

        var AuthorizationResource = camAPI.resource('authorization'),
            GroupResource         = camAPI.resource('group'),
            TenantResource        = camAPI.resource('tenant'),
            UserResource          = camAPI.resource('user');

        var encodeId = function(id) {
          return id
            .replace(/\//g, '%2F')
            .replace(/\\/g, '%5C');
        };

        $scope.encodedUserId = encodeId($routeParams.userId);

        var refreshBreadcrumbs = function() {
          page.breadcrumbsClear();
          page.breadcrumbsAdd({
            label: 'Users',
            href: '#/users/'
          });
        };

        $scope.encodedUserId = encodeId($routeParams.userId);
        $scope.decodedUserId = $routeParams.userId
                                                .replace(/%2F/g, '/')
                                                .replace(/%5C/g, '\\');
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

        UserResource.options({ id : $scope.encodedUserId }, function(err, res) {
          angular.forEach(res.links, function(link) {
            $scope.availableOperations[link.rel] = true;
          });
        });

        // update profile form /////////////////////////////

        var loadProfile = $scope.loadProfile = function() {
          UserResource.profile({ id : $scope.encodedUserId }, function(err, res) {
            $scope.user = res;

            $scope.profile = angular.copy(res);
            $scope.profileCopy = angular.copy(res);

            page.titleSet('Edit `' + $scope.user + '` user');

            refreshBreadcrumbs();

            page.breadcrumbsAdd({
              label: [$scope.user.firstName, $scope.user.lastName].filter(function(v) { return !!v; }).join(' '),
              href: '#/users/' + $scope.user.id
            });
          });
        };

        $scope.updateProfile = function() {
          var resourceData = angular.extend({}, { id : $scope.encodedUserId }, $scope.profile);
          UserResource.updateProfile(resourceData, function(err) {
            if( err === null ) {
              Notifications.addMessage({
                type : 'success',
                status : 'Success',
                message : 'User profile successfully updated.'
              });
              loadProfile();

            } else {

              Notifications.addError({
                status : 'Failed',
                message : 'Failed to update user profile'
              });
            }
          });
        };
        // update password form ////////////////////////////

        var resetCredentials = function() {
          $scope.credentials.authenticatedUserPassword = '';
          $scope.credentials.password = '';
          $scope.credentials.password2 = '';

          $scope.updateCredentialsForm.$setPristine();
        };

        $scope.updateCredentials = function() {
          var credentialsData = {
            authenticatedUserPassword: $scope.credentials.authenticatedUserPassword,
            password: $scope.credentials.password
          };

          var resourceData = angular.extend({}, { id : $scope.encodedUserId }, credentialsData);

          UserResource.updateCredentials(resourceData, function(err) {
            if( err === null ) {
              Notifications.addMessage({
                type: 'success',
                status: 'Password',
                message: 'Changed the password.',
                duration: 5000,
                exclusive: true
              });
              resetCredentials();

            } else {
              if( err.status === 400 ) {
                if( $scope.decodedUserId === $scope.authenticatedUser ) {
                  Notifications.addError({
                    status : 'Password',
                    message : 'Old password is not valid.',
                    exclusive : true
                  });

                } else {
                  Notifications.addError({
                    status : 'Password',
                    message : 'Your password is not valid.',
                    exclusive : true
                  });
                }
              } else {
                Notifications.addError({
                  status : 'Password',
                  message : 'Could not change the password.'
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
              $dialogScope.question = 'Really delete user ' + $scope.user.id + '?';
            }]
          }).result.then(function() {
            UserResource.delete({ id: $scope.encodedUserId }, function() {
              Notifications.addMessage({
                type: 'success',
                status: 'Success',
                message: 'User '+$scope.user.id+' successfully deleted.'
              });
              $location.path('/users');
            }
            );
          });
        };

        // group form /////////////////////////////

        $scope.$watch(function() {
          return $location.search().tab === 'groups';
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
          var encodedGroupId = encodeId(groupId);

          GroupResource.deleteMember({ userId: $scope.encodedUserId, id: encodedGroupId}, function() {
            Notifications.addMessage({
              type:'success',
              status:'Success',
              message:'User '+$scope.user.id+' removed from group.'
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

        $scope.$watch(function() {
          return $location.search().tab === 'tenants';
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
          var encodedTenantId = encodeId(tenantId);

          TenantResource.deleteUserMember({userId: $scope.encodedUserId, id: encodedTenantId}, function() {
            Notifications.addMessage({
              type:'success',
              status:'Success',
              message:'User '+$scope.user.id+' removed from tenant.'
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
                return $scope.encodedUserId;
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

        page.titleSet('Edit user');
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
