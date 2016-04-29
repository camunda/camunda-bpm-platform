'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/authorizations.html', 'utf8');

module.exports = [
  'ViewsProvider',
function (
  ViewsProvider
) {
  ViewsProvider.registerDefaultView('admin.dashboard.section', {
    id: 'authorizations',
    label: 'Authorizations',
    template: template,
    pagePath: '#/authorization',
    controller: [function() {}],
    priority: 0
  });
}];
