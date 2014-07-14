'use strict';

define(['angular'], function(angular) {

  var module = angular.module('admin.pages');

  var Controller = [   
    '$scope', 
    '$location',
    '$routeParams',
    'Views',     
    function($scope, 
             $location, 
             $routeParams,
             Views) {      

    $scope.systemSettingsProviders = Views.getProviders({ component: 'admin.system'});

    var selectedProviderId = $routeParams.section;
    if(!!selectedProviderId) {
      $scope.activeSettingsProvier = Views.getProviders({ 
        component: 'admin.system',
        id: $routeParams.section
      })[0];
    }


    $scope.activeClass = function(link) {
      var path = $location.absUrl();
      return path.indexOf(link) != -1 ? "active" : "";
    };

  }];

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/system', {
      templateUrl: require.toUrl('./app/admin/pages/system.html'),
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
      }
    });
  }];

  module
    .config(RouteConfig);

});
