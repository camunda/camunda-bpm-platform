define([
  'angular',
  'text!./cam-cockpit-details-plugin.html',
], function(
  angular,
  template
) {
  'use strict';

  var Controller = [
   '$scope',
  function (
    $scope
  ) {

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cam.cockpit.repository.resouce.detail.not.valid', {
      id: 'resource-details',
      label: 'Details',
      template: template,
      controller: Controller,
      priority: 1000
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
