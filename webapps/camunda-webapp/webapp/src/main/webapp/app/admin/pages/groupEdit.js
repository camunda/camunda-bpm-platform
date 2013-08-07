'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', '$routeParams', 'GroupResource', 'AuthorizationResource', 'Notifications', '$location', 
    function ($scope, $routeParams, GroupResource, AuthorizationResource, Notifications, $location) {

    $scope.group = null;
    $scope.groupName = null;
    $scope.groupId = $routeParams.groupId;

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
      GroupResource.get({groupId : $routeParams.groupId}).$then(function(response) {
        $scope.group = response.data;
        $scope.groupName = (!!response.data.name ? response.data.name : response.data.id);
        $scope.groupCopy = angular.copy(response.data);
      });
    }

    GroupResource.OPTIONS({groupId : $routeParams.groupId}).$then(function(response) {
      angular.forEach(response.data.links, function(link){
        $scope.availableOperations[link.rel] = true;
      });    
    });

    $scope.updateGroup = function() {      

      GroupResource.update($scope.group).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"Group successfully updated."});
          loadGroup();
        },
        function(){
          Notifications.addError({type:"error", status:"Error", message:"Could not update group."});
        }
      );
    }

    // delete group form /////////////////////////////

    $scope.deleteGroup = function() {
      GroupResource.delete({'groupId':$scope.group.id}).$then(
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

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/groups/:groupId', {
      templateUrl: 'pages/groupEdit.html',
      controller: Controller,
      reloadOnSearch: false
    });

    // multi tenacy
    $routeProvider.when('/:engine/groups/:groupId', {
      templateUrl: 'pages/groupEdit.html',
      controller: Controller,
      reloadOnSearch: false
    });
  }];

  module
    .config(RouteConfig);

});
