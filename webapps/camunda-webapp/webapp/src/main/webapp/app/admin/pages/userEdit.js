'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', '$routeParams', 'UserResource', 'GroupResource', 'GroupMembershipResource', 'Notifications', '$location', 'AuthorizationResource',
    function ($scope, $routeParams, UserResource, GroupResource, GroupMembershipResource, Notifications, $location, AuthorizationResource) {

    $scope.userId = $routeParams.userId;

    // used to display information about the user
    $scope.profile = null;

    // data model for the profile form (profileCopy is used for dirty checking)
    $scope.profile = null;
    $scope.profileCopy = null;

    // data model for the changePassword form 
    $scope.credentials = {
        password : "",
        password2 : ""
    };

    // list of the user's groups
    $scope.groupList = null;
    $scope.groupIdList = null;

    $scope.createGroupMembershipDialog = new Dialog();

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
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"User profile successfully updated."});
          loadProfile();
        }
      );
    }

    // update password form ////////////////////////////

    var resetCredentials = function() {
      $scope.credentials.password = "";
      $scope.credentials.password2 = "";    
      $scope.updateCredentialsForm.$setPristine();      
    }

    $scope.updateCredentials = function() {    
      UserResource.updateCredentials({'userId':$scope.user.id},{'password' : $scope.credentials.password}).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"Password successfully changed."});
          resetCredentials();
        }
      );
    }

    // delete user form /////////////////////////////

    $scope.deleteUser = function() {
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
      $scope.createGroupMembershipDialog.open();
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

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/users/:userId', {
      templateUrl: 'pages/userEdit.html',
      controller: Controller,
      reloadOnSearch: false
    });

    // multi tenacy
    $routeProvider.when('/:engine/users/:userId', {
      templateUrl: 'pages/userEdit.html',
      controller: Controller,
      reloadOnSearch: false
    });
  }];

  module
    .config(RouteConfig);

});
