"use strict";

define(["angular"], function(angular, BpmnRender) {

  var module = angular.module("cockpit.pages");

  var Controller = function($scope, $routeParams, ProcessInstanceResource, $location, Views) {

    $scope.processDefinitionId = $routeParams.processDefinitionId;
    $scope.processInstanceId = $routeParams.processInstanceId;

    ProcessInstanceResource.activityInstances(
      {
        id: $scope.processInstanceId
      })
      .$then(function(result) {
        $scope.activityInstances = result.data;
      });

    $scope.processInstanceVars = { read: [ 'processInstanceId' ] };
    $scope.processInstanceViews = Views.getProviders({ component: 'cockpit.processInstance.instanceDetails' });

    $scope.selectView = function(view) {
      $scope.selectedView = view;
      $location.search('detailsTab', view.id);
    };

    function setDefaultTab(tabs) {
      var selectedTabId = $location.search().detailsTab;

      if (!tabs.length) {
        return;
      }

      if (selectedTabId) {
        var provider = Views.getProvider({ component: 'cockpit.processInstance.instanceDetails', id: selectedTabId });
        if (provider && tabs.indexOf(provider) != -1) {
          $scope.selectedView = provider;
          return;
        }
      }

      $location.search('detailsTab', null);
      $scope.selectedView = tabs[0];
    }

    setDefaultTab($scope.processInstanceViews);
  };

  Controller.$inject = [ '$scope', '$routeParams', 'ProcessInstanceResource', '$location', 'Views' ];

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
