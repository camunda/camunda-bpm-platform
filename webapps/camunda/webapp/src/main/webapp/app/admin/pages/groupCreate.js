'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = ['$scope', 'GroupResource', 'Notifications', '$location', function ($scope, GroupResource, Notifications, $location) {

    // data model for new group
    $scope.group = {
      id : "",
      name : "",
      type : ""
    }

    $scope.createGroup = function() {
      var group = $scope.group;
      GroupResource.createGroup(group).$then(
        function(){
          Notifications.addMessage({type:"success", status:"Success", message:"Successfully created new group "+group.id});
          $location.path("/groups");
        }
      );
    }

  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/group-create', {
      templateUrl: 'pages/groupCreate.html',
      controller: Controller
    });

    // multi tenacy
    $routeProvider.when('/:engine/group-create', {
      templateUrl: 'pages/groupCreate.html',
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});