'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/pd-incidents-tab.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
    id: 'pd-incidents-tab',
    label: 'Incidents',
    template: template,
    priority: 9,
    controller: function() {}
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
