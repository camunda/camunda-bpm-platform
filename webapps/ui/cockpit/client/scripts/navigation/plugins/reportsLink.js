'use strict';

var fs = require('fs');

var reportsLinkTemplate = fs.readFileSync(__dirname + '/reportsLink.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.navbar.action', {
    id: 'reports',
    template: reportsLinkTemplate,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
