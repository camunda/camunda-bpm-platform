'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/systemSettingsGeneral.html', 'utf8');

var Controller = [
  '$scope',
  'Uri',
  function($scope, Uri) {

    $scope.processEngineName = Uri.appUri(':engine');


  }];

module.exports = ['ViewsProvider', function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('admin.system', {
    id: 'system-settings-general',
    label: 'General',
    template: template,
    controller: Controller,
    priority: 1000
  });
}];
