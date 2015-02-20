define(['angular', 'text!./groups.html'], function(angular, template) {
  'use strict';
  var Controller = ['$scope', 'GroupResource', function ($scope, GroupResource) {

    $scope.availableOperations={};
    $scope.loadingState = 'LOADING';

    GroupResource.query().$promise.then(function(response) {
      $scope.groupList = response;
      $scope.loadingState = response.length ? 'LOADED' : 'EMPTY';
    });

    GroupResource.OPTIONS().$promise.then(function(response) {
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
