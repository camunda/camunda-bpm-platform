'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', '$routeParams', 'GroupResource', 'AuthorizationResource', 'Notifications', '$location', '$window',
    function ($scope, $routeParams, GroupResource, AuthorizationResource, Notifications, $location, $window) {

    $scope.group = null;
    $scope.groupName = null;
    $scope.encodedGroupId = $routeParams.groupId.replace(/\//g, '%2F');

    $scope.availableOperations = {};

    // common form validation //////////////////////////

    /** form must be valid & user must have made some changes */
    var canSubmit = $scope.canSubmit = function(form, modelObject) {
      return form.$valid
        && !form.$pristine
        && (modelObject == null || !angular.equals($scope[modelObject], $scope[modelObject+'Copy']));
    };

    // update group form /////////////////////////////

    var loadGroup = $scope.loadGroup = function() {
      GroupResource.get({groupId : $scope.encodedGroupId}).$promise.then(function(response) {
        // $scope.group = response.data;
        // $scope.groupName = (!!response.data.name ? response.data.name : response.data.id);
        // $scope.groupCopy = angular.copy(response.data);
        $scope.group = response;
        $scope.groupName = (!!response.name ? response.name : response.id);
        $scope.groupCopy = angular.copy(response);
      });
    }

    GroupResource.OPTIONS({groupId : $scope.encodedGroupId}).$promise.then(function(response) {
      // angular.forEach(response.data.links, function(link){
      angular.forEach(response.links, function(link){
        $scope.availableOperations[link.rel] = true;
      });
    });

    $scope.updateGroup = function() {

      GroupResource.update({groupId: $scope.encodedGroupId}, $scope.group).$promise.then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"Group successfully updated."});
          loadGroup();
        },
        function() {
          Notifications.addError({ status: "Failed", message: "Failed to update group" });
        }
      );
    }

    // delete group form /////////////////////////////

    $scope.deleteGroup = function() {

      function confirmDelete() {
        return $window.confirm('Really delete group ' + $scope.group.id + '?');
      }

      if (!confirmDelete()) {
        return;
      }

      GroupResource.delete({'groupId':$scope.encodedGroupId}).$promise.then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"Group "+$scope.group.id+" successfully deleted."});
          $location.path("/groups");
        }
      );
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

    loadGroup();

    if(!$location.search().tab) {
      $location.search({'tab': 'group'});
      $location.replace();
    }

  }];

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/groups/:groupId*', {
      templateUrl: require.toUrl('./app/admin/pages/groupEdit.html'),
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
