'use strict';

/* Plugin Services */

define([ "angular" ], function(angular) {

  var module = angular.module("cockpit.plugin");

  function PluginsProvider() {
    var defaultPluginMap = {};
    var extensionPluginMap = {};

    var initialized = false;

    function internalRegisterPlugin(key, plugin, map) {
      // make sure map is initialized
      var pluginsByKey = map[key] = map[key] || [];

      pluginsByKey.push(plugin);
    }

    /**
     * Initializes plugin map to replace prefixes in templates
     *
     * @param map the plugin map
     * @param app the application to resolve plugin references against
     */
    function initializePlugins(map, Uri) {
      angular.forEach(map, function(plugins) {
        angular.forEach(plugins, function(plugin) {

          if (plugin.url) {
            plugin.url = Uri.appUri(plugin.url);
          }
        });
      });
    }

    this.registerDefaultPlugin = function(key, plugin) {
      internalRegisterPlugin(key, plugin, defaultPluginMap);
    };

    this.registerPlugin = function(key, plugin) {
      internalRegisterPlugin(key, plugin, extensionPluginMap);
    };

    this.$get = ['Uri', '$filter', function(Uri, $filter) {
      var plugins = {
        get: function(options, dynamic) {
          if (!initialized) {
            initializePlugins(defaultPluginMap, Uri);
            initializePlugins(extensionPluginMap, Uri);

            initialized = true;
          }

          var component = options.component;
          if (!component) {
            throw new Error("No component given");
          }

          var autoActivatedDynamicPlugins = dynamic || [];
          if (!angular.isArray(autoActivatedDynamicPlugins)) {
            throw new Error("Argument dynamic must be an array");
          }

          // load default plugins
          var plugins = angular.copy(defaultPluginMap[component]);

          function getPluginById(id, plugins) {
            var filtered = $filter('filter')(plugins, { id: id });
            if (filtered.length) {
              return filtered[0];
            } else {
              return null;
            }
          }

          function addPlugin(plugins, plugin) {
            var addPluginPriority = plugin.priority || 0;

            // check from right to left (*-) where plugin
            // should be added
            for (var i = plugins.length - 1; i >= 0; i--) {
              var p = plugins[i];
              if (!p.priority || p.priority < addPluginPriority) {
                plugins.splice(i + 1, 0, plugin);
                return;
              }
            }

            // not yet added; add to front
            plugins.splice(0, 0, plugin);
          }

          function replacePlugin(plugins, id, newPlugin) {
            for (var i = 0; i < plugins.length; i++) {
              var plugin = plugins[i];
              if (plugin.id == id) {
                plugins.splice(i, 1, newPlugin);
                return true;
              }
            }

            return false;
          }

          // add client side activated dynamic plugins
          angular.forEach(autoActivatedDynamicPlugins, function(pluginId) {
            var plugin = getPluginById(pluginId, extensionPluginMap[component]);
            if (plugin) {
              addPlugin(plugins, plugin);
            } else {
              console.log("[warn] enabled plugin not loaded: ", pluginId);
            }
          });

          return plugins;
        }
      };

      return plugins;
    }];
  }

  module.provider('Plugins', PluginsProvider);
  // end config

  return module;
});
