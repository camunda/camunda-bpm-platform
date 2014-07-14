'use strict';

ngDefine('admin.pages', function(module) {

  var Controller = [
   '$scope',
   'Uri',
  function ($scope, Uri) {

    $scope.processEngineName = Uri.appUri(":engine");

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('admin.system', {
      id: 'system-settings-general',
      label: 'General',
      url: 'app://pages/systemSettingsGeneral.html',
      controller: Controller,
      priority: 1000
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});

