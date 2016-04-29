'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tenants.html', 'utf8');

module.exports = [
  'ViewsProvider',
function (
  ViewsProvider
) {
  ViewsProvider.registerDefaultView('admin.dashboard.section', {
    id: 'tenants',
    label: 'Tenants',
    template: template,
    pagePath: '#/tenants',
    controller: [function() {}],

    priority: 0
  });
}];
