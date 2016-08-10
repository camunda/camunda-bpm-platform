'use strict';

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('tasklist.header', {
    id: 'tasklist-sorting',
    template: '<div cam-sorting-choices tasklist-data="tasklistData"></div>',
    controller: function() {},
    priority: 200
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;