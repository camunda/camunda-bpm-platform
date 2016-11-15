'use strict';

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.navigation', {
    id: 'reports',
    label: 'Reports',
    pagePath: '#/reports',
    template: '<!-- nothing to show, but needed -->',
    controller: function() {},
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
