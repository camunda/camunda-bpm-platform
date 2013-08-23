define([ 'angular', 'require' ], function(angular, require) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', '$window', '$routeParams', 'UserResource', 'GroupResource', 'GroupMembershipResource', 'Notifications', '$location', '$dialog', 'AuthorizationResource', 'authenticatedUser',
    function ($scope, $window, $routeParams, UserResource, GroupResource, GroupMembershipResource, Notifications, $location, $dialog, AuthorizationResource, authenticatedUser) {

    $scope.userId = $routeParams.userId;
    $scope.authenticatedUser = authenticatedUser;

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
    var canSubmit = $scope.canSubmit = function(form, modelObject) {
      return form.$valid
        && !form.$pristine
        && (modelObject == null || !angular.equals($scope[modelObject], $scope[modelObject+'Copy']));
    };

    // load options ////////////////////////////////////

    UserResource.OPTIONS({userId : $scope.userId}).$then(function(response) {
      angular.forEach(response.data.links, function(link){
        $scope.availableOperations[link.rel] = true;
      });
    });

    // update profile form /////////////////////////////

    var loadProfile = $scope.loadProfile = function() {
      UserResource.profile({userId : $scope.userId}).$then(function(response) {
        $scope.user = response.data;
        $scope.profile = angular.copy(response.data);
        $scope.profileCopy = angular.copy(response.data);
      });
    }

    $scope.updateProfile = function() {

      UserResource.updateProfile($scope.profile).$then(
        function() {
          Notifications.addMessage({type:"success", status:"Success", message:"User profile successfully updated."});
          loadProfile();
        },
        function() {
          Notifications.addError({ status: "Failed", message: "Failed to update user profile" });
        }
      );
    }

    // update password form ////////////////////////////

    var resetCredentials = function() {
      $scope.credentials.authenticatedUserPassword = '';
      $scope.credentials.password = '';
      $scope.credentials.password2 = '';

      $scope.updateCredentialsForm.$setPristine();
    }

    $scope.updateCredentials = function() {
      var pathParams = { userId: $scope.user.id },
          params = {authenticatedUserPassword: $scope.credentials.authenticatedUserPassword, password: $scope.credentials.password };

      UserResource.updateCredentials(pathParams, params).$then(

        function() {
          Notifications.addMessage({ type: "success", status: "Password", message: "Changed the password.", duration: 5000, exclusive: true });
          resetCredentials();
        },

        function(error) {
          if (error.status === 400) {
            if ($scope.userId === $scope.authenticatedUser) {
              Notifications.addError({ status: "Password", message: "Old password is not valid.", exclusive: true });
            } else {
              Notifications.addError({ status: "Password", message: "Your password is not valid.", exclusive: true });
            }
          } else {
            Notifications.addError({ status: "Password", message: "Could not change the password." });  
          }
        });
    }

    // delete user form /////////////////////////////

    $scope.deleteUser = function() {

      function confirmDelete() {
        return $window.confirm('Really delete user ' + $scope.user.id + '?');
      }

      if (!confirmDelete()) {
        return;
      }

      UserResource.delete({'userId':$scope.user.id}).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"User "+$scope.user.id+" successfully deleted."});
          $location.path("/users");
        }
      );
    }

    // group form /////////////////////////////

    var loadGroups = $scope.loadGroups = function() {
      GroupResource.query({'member' : $routeParams.userId}).$then(function(response) {
        $scope.groupList = response.data;
        $scope.groupIdList = [];
        angular.forEach($scope.groupList, function(group) {
          $scope.groupIdList.push(group.id);
        });
      });
    }

    $scope.removeGroup = function(groupId) {
      GroupMembershipResource.delete({'userId':$scope.user.id, 'groupId': groupId}).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"User "+$scope.user.id+" removed from group."});
          loadGroups();
        }
      );
    }

    $scope.openCreateGroupMembershipDialog = function() {
      var dialog = $dialog.dialog({
        controller: 'GroupMembershipDialogController',
        templateUrl: require.toUrl('./create-group-membership.html'),
        resolve: {
          user: function() {
            return $scope.user;
          },
          groupIdList: function() {
            return $scope.groupIdList;
          }
        }
      });

      dialog.open().then(function(result) {

        if (result == "SUCCESS") {
          $scope.loadGroups();
        }
      });
    }

    function checkRemoveGroupMembershipAuthorized() {
      AuthorizationResource.check({permissionName:"DELETE", resourceName:"group membership", resourceType:3}).$then(function(response) {
        $scope.availableOperations.removeGroup = response.data.authorized;
      });
    }

    // page controls ////////////////////////////////////

    $scope.show = function(fragment) {
      return fragment == $location.search().tab;
    };

    $scope.activeClass = function(link) {
      var path = $location.absUrl();
      return path.indexOf(link) != -1 ? "active" : "";
    };

    // initialization ///////////////////////////////////

    loadProfile();
    loadGroups();
    checkRemoveGroupMembershipAuthorized();

    if(!$location.search().tab) {
      $location.search({'tab': 'profile'});
      $location.replace();
    }

  }];

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/users/:userId', {
      templateUrl: 'pages/userEdit.html',
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
      },
      reloadOnSearch: false
    });
  }];

  module
    .config(RouteConfig);

});
