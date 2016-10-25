'use strict';

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'proceses',
    label: 'Processes',
    pagePath: '#/processes',
    checkActive: function(path) {
    // matches "#/process/", "#/processes" or "#/migration"
      return path.indexOf('#/process') > -1 || path.indexOf('#/migration') > -1;
    },
    template: '<!-- nothing to show, but needed -->',
    controller: function() {},

    priority: 100
  });
}];
