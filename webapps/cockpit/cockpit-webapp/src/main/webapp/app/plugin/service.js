'use strict';

/* Plugin Services */

define([ "angular" ], function(angular) {

  var module = angular.module("cockpit.plugin");

  function ViewsProvider() {

    var defaultViewMap = {};
    var pluginViewMap = {};

    var initialized = false;

    function internalRegisterProvider(key, provider, map) {
      // make sure map is initialized
      var viewsByKey = map[key] = map[key] || [];

      addViewProvider(viewsByKey, provider);
    }

    function addViewProvider(viewProviders, provider) {
      var priority = provider.priority || 0;

      // check from right to left (*-) where plugin
      // should be added
      for (var i = 0, p; !!(p = viewProviders[i]); i++) {
        if (!p.priority || p.priority < priority) {
          viewProviders.splice(i, 0, provider);
          return;
        }
      }

      // not yet added; add to front
      viewProviders.push(provider);
    }

    /**
     * Initializes the view map to replace prefixes in templates
     *
     * @param map the plugin map
     * @param app the application to resolve plugin references against
     */
    function initializeViews(map, Uri) {
      angular.forEach(map, function(viewProviders) {
        angular.forEach(viewProviders, function(viewProvider) {

          if (viewProvider.url) {
            viewProvider.url = Uri.appUri(viewProvider.url);
          }
        });
      });
    }

    this.registerDefaultView = function(key, viewProvider) {
      internalRegisterProvider(key, viewProvider, defaultViewMap);
    };

    this.registerView = function(key, viewProvider) {
      internalRegisterProvider(key, viewProvider, defaultViewMap);
    };

    function ensureInitialized(Uri) {
      if (!initialized) {
        initializeViews(defaultViewMap, Uri);
        initializeViews(pluginViewMap, Uri);

        initialized = true;
      }
    }

    this.$get = ['Uri', '$filter', function(Uri, $filter) {
      var service = {

        getProviders: function(options) {
          ensureInitialized(Uri);

          var component = options.component;
          if (!component) {
            throw new Error("No component given");
          }

          var viewProviders = defaultViewMap[component];
          return viewProviders || [];
        },

        getProvider: function(options) {

          var viewProviders = this.getProviders(options);

          function getViewById(id, viewProviders) {
            var filtered = $filter('filter')(viewProviders, { id: id });
            if (filtered.length) {
              return filtered[0];
            } else {
              return null;
            }
          }

          function replaceView(viewProviders, id, replaceViewProvider) {
            for (var i = 0; i < viewProviders.length; i++) {
              var p = viewProviders[i];
              if (p.id === id) {
                viewProviders.splice(i, 1, replaceViewProvider);
                return true;
              }
            }

            return false;
          }

          return (viewProviders || [])[0];
        }
      };

      return service;
    }];
  }

  module.provider('Views', ViewsProvider);
  // end config

  return module;
});
