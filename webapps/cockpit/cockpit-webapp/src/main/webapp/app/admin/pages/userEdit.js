'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', '$routeParams', 'UserResource', 'GroupResource', 'GroupMembershipResource', 'Notifications', '$location', function ($scope, $routeParams, UserResource, GroupResource, GroupMembershipResource, Notifications, $location) {

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

    // common form validation //////////////////////////

    /** form must be valid & user must have made some changes */
    var canSubmit = $scope.canSubmit = function(form, modelObject) {
      return form.$valid 
        && !form.$pristine
        && (modelObject == null || !angular.equals($scope[modelObject], $scope[modelObject+'Copy']));
    };

    // update profile form /////////////////////////////

    var loadProfile = $scope.loadProfile = function() {
      UserResource.profile({userId : $routeParams.userId}).$then(function(response) {
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
        },
        function(){
          Notifications.addError({type:"error", status:"Error", message:"Could not update user profile."});
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
        },
        function(){
          Notifications.addError({type:"error", status:"Error", message:"Could not change user password."});
        }
      );
    }

    // delete user form /////////////////////////////

    $scope.deleteUser = function() {
      UserResource.delete({'userId':$scope.user.id}).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"User "+$scope.user.id+" successfully deleted."});
          $location.path("/users");
        },
        function(){
          Notifications.addError({type:"error", status:"Error", message:"Could not delete user "+$scope.user.id+"."});
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
        },
        function(){
          Notifications.addError({type:"error", status:"Error", message:"Could not remove user from group."});
        }
      );
    }

    $scope.openCreateGroupMembershipDialog = function() {
      $scope.createGroupMembershipDialog.open();
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
