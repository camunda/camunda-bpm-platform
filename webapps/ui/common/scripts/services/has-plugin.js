'use strict';
module.exports = ['Views', function(Views) {
  return function(pluginPoint, pluginId) {
    return !!Views.getProviders({component: pluginPoint})
            .filter(function(plugin) {
              return plugin.id === pluginId;
            }).length;
  };
}];
