'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-resource-action-download-plugin.html', 'utf8');

var Controller = [
  '$scope',
  function(
    $scope
  ) {

    // fields ////////////////////////////////////////////

    var downloadData = $scope.resourceDetailsData.newChild($scope);


    // observe //////////////////////////////////////////

    downloadData.observe('resource', function(_resource) {
      $scope.resource = _resource;
    });

    downloadData.observe('currentDeployment', function(_deployment) {
      $scope.deployment = _deployment;
    });


    // download link /////////////////////////////////////

    $scope.downloadLink = $scope.control.downloadLink;

  }];

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.repository.resource.action', {
    id: 'download-resource',
    controller: Controller,
    template: template,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
