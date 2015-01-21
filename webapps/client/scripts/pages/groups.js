define(['angular', 'text!./groups.html'], function(angular, template) {
  'use strict';
  var Controller = ['$scope', 'GroupResource', function ($scope, GroupResource) {

    $scope.availableOperations={};

    // GroupResource.query().$promise.then(function(response) {
    GroupResource.query().$promise.then(function(response) {
      $scope.groupList = response;
    });

    GroupResource.OPTIONS().$promise.then(function(response) {
      // angular.forEach(response.data.links, function(link){
      angular.forEach(response.links, function(link){
        $scope.availableOperations[link.rel] = true;
      });
    });

  }];

  return [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/groups', {
      template: template,
      controller: Controller,
      authentication: 'required'
    });
  }];
});
