define([
  'angular',
  'text!./cam-cockpit-delete-deployment-plugin.html',
  'text!./modals/cam-cockpit-delete-deployment-modal.html',
], function(
  angular,
  template,
  modalTemplate
) {
  'use strict';

  var Controller = [
   '$scope',
   '$modal',
  function (
    $scope,
    $modal
  ) {

    var deploymentData = $scope.deploymentData;
    var deployment = $scope.deployment;

    $scope.deleteDeployment = function ($event, deployment) {
      $event.stopPropagation();

      $modal.open({
        controller: 'camDeleteDeploymentModalCtrl',
        template: modalTemplate,
        resolve: {
          'deploymentData': function() { return deploymentData; },
          'deployment': function() { return deployment; }
        }
      }).result.then(function() {
        deploymentData.changed('deployments');
      });

    };

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.repository.deployment.action', {
      id: 'delete-deployment',
      template: template,
      controller: Controller,
      priority: 1000
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
