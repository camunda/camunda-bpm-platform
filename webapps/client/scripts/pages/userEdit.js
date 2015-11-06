/* global define: false */
define([
  'angular',
  'text!./userEdit.html',
  'text!./create-group-membership.html',
  'text!./generic-confirmation.html'
], function(
  angular,
  template,
  groupTemplate,
  confirmationTemplate
) {
  'use strict';
  return [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/users/:userId', {
      template: template,
      controller: [
              '$scope', '$routeParams', 'UserResource', 'GroupResource', 'GroupMembershipResource', 'Notifications', '$location', '$modal', 'AuthorizationResource', 'authentication',
      function($scope,   $routeParams,   UserResource,   GroupResource,   GroupMembershipResource,   Notifications,   $location,   $modal,   AuthorizationResource,   authentication) {

        $scope.encodedUserId = $routeParams.userId
                                                .replace(/\//g, '%2F')
                                                .replace(/\\/g, '%5C');
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

        UserResource.OPTIONS({userId : $scope.encodedUserId}).$promise.then(function(response) {
          // angular.forEach(response.data.links, function(link){
          angular.forEach(response.links, function(link){
            $scope.availableOperations[link.rel] = true;
          });
        });

        // update profile form /////////////////////////////

        var loadProfile = $scope.loadProfile = function() {
          UserResource.profile({userId : $scope.encodedUserId}).$promise.then(function(response) {
            // console.warn('not sure it blends', response);
            $scope.user = response;
            // $scope.profile = angular.copy(response.data);
            // $scope.profileCopy = angular.copy(response.data);
            $scope.profile = angular.copy(response);
            $scope.profileCopy = angular.copy(response);
          });
        };

        $scope.updateProfile = function() {

          UserResource.updateProfile({userId: $scope.encodedUserId}, $scope.profile).$promise.then(
            function() {
              Notifications.addMessage({type:'success', status:'Success', message:'User profile successfully updated.'});
              loadProfile();
            },
            function() {
              Notifications.addError({ status: 'Failed', message: 'Failed to update user profile' });
            }
          );
        };

        // update password form ////////////////////////////

        var resetCredentials = function() {
          $scope.credentials.authenticatedUserPassword = '';
          $scope.credentials.password = '';
          $scope.credentials.password2 = '';

          $scope.updateCredentialsForm.$setPristine();
        };

        $scope.updateCredentials = function() {
          var pathParams = { userId: $scope.encodedUserId },
              params = {authenticatedUserPassword: $scope.credentials.authenticatedUserPassword, password: $scope.credentials.password };

          UserResource.updateCredentials(pathParams, params).$promise.then(

            function() {
              Notifications.addMessage({ type: 'success', status: 'Password', message: 'Changed the password.', duration: 5000, exclusive: true });
              resetCredentials();
            },

            function(error) {
              if (error.status === 400) {
                if ($scope.decodedUserId === $scope.authenticatedUser) {
                  Notifications.addError({ status: 'Password', message: 'Old password is not valid.', exclusive: true });
                } else {
                  Notifications.addError({ status: 'Password', message: 'Your password is not valid.', exclusive: true });
                }
              } else {
                Notifications.addError({ status: 'Password', message: 'Could not change the password.' });
              }
            });
        };

        // delete user form /////////////////////////////

        $scope.deleteUser = function() {
          $modal.open({
            template: confirmationTemplate,
            controller: ['$scope', function ($dialogScope) {
              $dialogScope.question = 'Really delete user ' + $scope.user.id + '?';
            }]
          }).result.then(function () {
            UserResource.delete({'userId':$scope.encodedUserId}).$promise.then(
              function(){
                Notifications.addMessage({type:'success', status:'Success', message:'User '+$scope.user.id+' successfully deleted.'});
                $location.path('/users');
              }
            );
          });
        };

        // group form /////////////////////////////

        var loadGroups = $scope.loadGroups = function() {
          GroupResource.query({'member' : $scope.decodedUserId}).$promise.then(function(response) {
            // $scope.groupList = response.data;
            $scope.groupList = response;
            $scope.groupIdList = [];
            angular.forEach($scope.groupList, function(group) {
              $scope.groupIdList.push(group.id);
            });
          });
        };

        $scope.removeGroup = function(groupId) {
          var encodedGroupId = groupId
                                    .replace(/\//g, '%2F')
                                    .replace(/\\/g, '%5C');
          GroupMembershipResource.delete({'userId':$scope.encodedUserId, 'groupId': encodedGroupId}).$promise.then(
            function(){
              Notifications.addMessage({type:'success', status:'Success', message:'User '+$scope.user.id+' removed from group.'});
              loadGroups();
            }
          );
        };

        $scope.openCreateGroupMembershipDialog = function() {
          var dialog = $modal.open({
            controller: 'GroupMembershipDialogController',
            template: groupTemplate,
            resolve: {
              user: function() {
                return $scope.user;
              },
              userId: function() {
                return $scope.encodedUserId;
              },
              groupIdList: function() {
                return $scope.groupIdList;
              }
            }
          });

          dialog.result.then(function(result) {

            if (result == 'SUCCESS') {
              $scope.loadGroups();
            }
          });
        };

        function checkRemoveGroupMembershipAuthorized() {
          AuthorizationResource.check({permissionName:'DELETE', resourceName:'group membership', resourceType:3}).$promise.then(function(response) {
            // $scope.availableOperations.removeGroup = response.data.authorized;
            $scope.availableOperations.removeGroup = response.authorized;
          });
        }

        // page controls ////////////////////////////////////

        $scope.show = function(fragment) {
          return fragment == $location.search().tab;
        };

        $scope.activeClass = function(link) {
          var path = $location.absUrl();
          return path.indexOf(link) != -1 ? 'active' : '';
        };

        // initialization ///////////////////////////////////

        loadProfile();
        loadGroups();
        checkRemoveGroupMembershipAuthorized();

        if(!$location.search().tab) {
          $location.search({'tab': 'profile'});
          $location.replace();
        }

      }],
      authentication: 'required',
      reloadOnSearch: false
    });
  }];
});
