'use strict';

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.navigation', {
    id: 'batch',
    label: 'Batches',
    pagePath: '#/batch',
    template: '<!-- nothing to show, but needed -->',
    controller: function() {},
    checkActive: function(path) {
      var ending = '#/batch';
      var parts = (path || '').split('?');
      return parts.length && (parts[0].slice(0 - ending.length) === ending);
    },
    priority: -5
  });
}];
