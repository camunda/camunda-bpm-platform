define(['text!./systemSettingsGeneral.html'], function(template) {
  'use strict';

  var Controller = [
   '$scope',
   'Uri',
  function ($scope, Uri) {

    $scope.processEngineName = Uri.appUri(":engine");


  }];

  return ['ViewsProvider', function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('admin.system', {
      id: 'system-settings-general',
      label: 'General',
      template: template,
      controller: Controller,
      priority: 1000
    });
  }];
});

