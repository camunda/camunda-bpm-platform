'use strict';

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('welcome.profile', {
    id: 'user-profile',
    template: '<div user-profile username="$root.authentication.name"></div>',
    controller: function() {},
    priority: 200
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;