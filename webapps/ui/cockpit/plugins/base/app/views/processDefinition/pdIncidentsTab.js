'use strict';

var fs = require('fs');

var incidentsTemplate = fs.readFileSync(__dirname + '/pd-incidents-tab.html', 'utf8');
var searchConfig = JSON.parse(fs.readFileSync(__dirname + '/pd-incidents-tab-config.json', 'utf8'));


var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
    id: 'pd-incidents-tab',
    label: 'Incidents',
    template: incidentsTemplate,
    priority: 15,
    controller: ['$scope', function($scope) {
      $scope.searchConfig = searchConfig;
    }]
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
