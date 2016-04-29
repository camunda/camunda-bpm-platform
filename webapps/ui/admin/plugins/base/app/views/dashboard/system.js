'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/system.html', 'utf8');

module.exports = [
  'ViewsProvider',
function (
  ViewsProvider
) {
  ViewsProvider.registerDefaultView('admin.dashboard.section', {
    id: 'system',
    label: 'System',
    template: template,
    pagePath: '#/system?section=system-settings-general',
    controller: [
      '$scope',
      'Views',
    function(
      $scope,
      Views
    ) {
      $scope.systemSettingsProviders = Views.getProviders({ component: 'admin.system'});
    }],

    priority: 0
  });
}];
