'use strict';

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.navigation', {
    id: 'batch',
    label: 'Batches',
    pagePath: '#/batch',
    template: '<!-- nothing to show, but needed -->',
    controller: function() {},
    checkActive: function(path) {
      return path && path.split('?')[0] && path.split('?')[0].endsWith('#/batch');
    },
    priority: -5
  });
}];
