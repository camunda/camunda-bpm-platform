'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/users.html', 'utf8');

module.exports = [
  'ViewsProvider',
function (
  ViewsProvider
) {
  ViewsProvider.registerDefaultView('admin.dashboard.section', {
    id: 'users',
    label: 'Users',
    template: template,
    pagePath: '#/users',
    controller: [function() {}],
    priority: 0
  });
}];
