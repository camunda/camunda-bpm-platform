"use strict";

define(["angular"], function(angular, BpmnRender) {

  var module = angular.module("cockpit.pages");

  var Controller = function($scope, $routeParams) {

    $scope.processDefinitionId = $routeParams.processDefinitionId;
    $scope.processInstanceId = $routeParams.processInstanceId;

  };

  Controller.$inject = [ '$scope', '$routeParams' ];

  var RouteConfig = function ($routeProvider) {
    $routeProvider.when('/process-definition/:processDefinitionId/process-instance/:processInstanceId', {
      templateUrl: 'pages/process-instance.html',
      controller: Controller,
      reloadOnSearch: false
    });
  };

  RouteConfig.$inject = ['$routeProvider'];

  module
    .config(RouteConfig);

});
