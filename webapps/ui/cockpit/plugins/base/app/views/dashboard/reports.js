'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/reports.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'reports',
    label: 'Reports',
    pagePath: '#/reports',
    template: template,
    controller: [
      '$scope',
      'Views',
      function($scope, Views) {
        $scope.plugins = Views.getProviders({
          component: 'cockpit.report'
        });
      }],
    access: [
      'Views',
      function(
      Views
    ) {
        return function(cb) {
          var reportPlugins = Views.getProviders({
            component: 'cockpit.report'
          });
          cb(null, !!reportPlugins.length);
        };
      }],

    priority: -4
  });
}];
