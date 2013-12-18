'use strict';

/* Plugin Services */

ngDefine('cockpit.plugin', [ 'angular' ], function(module, angular) {

  var PluginsProvider = [ function() {

    var pluginMap = {};

    function addPlugin(plugins, definition) {
      var priority = definition.priority || 0;

      // check from right to left (*-) where plugin
      // should be added
      for (var i = 0, p; !!(p = plugins[i]); i++) {
        if (!p.priority || p.priority < priority) {
          plugins.splice(i, 0, definition);
          return;
        }
      }

      // not yet added; add to front
      plugins.push(definition);
    }

    function internalRegisterPlugin(key, definition, map) {
      // make sure map is initialized
      var pluginsByKey = map[key] = map[key] || [];
      addPlugin(pluginsByKey, definition);
    }

    this.registerPlugin = function(type, key, definition) {
      var pluginTypeMap = pluginMap[type] = pluginMap[type] || {};
      internalRegisterPlugin(key, definition, pluginTypeMap);
    };

    this.$get = [ '$filter', function($filter) {
      var service = {
        getAllProviders: function(type) {
          return pluginMap[type] || {};
        },

        getProviders: function(type, options) {

          if (!type) {
            throw new Error('No type given');
          }

          var component = options.component;
          if (!component) {
            throw new Error('No component given');
          }

          var providers = (pluginMap[type] || {})[component];

          // filter by id and other filter criterias
          if (options.id) {
            providers = $filter('filter')(providers, { id: options.id });
          }

          return providers || [];
        },

        getProvider: function(type, options) {
          var providers = this.getProviders(type, options);
          return (providers || [])[0];
        }
      };

      return service;
    }];
  }];

  module.provider('Plugins', PluginsProvider);

  var ViewsProvider = [ 'PluginsProvider', function(PluginsProvider) {

    /**
     * Registers the given viewProvider for the specified view
     *
     * View provider is an object like the following:
     *
     * <pre>
     *   {
     *     id: String, // id if the view
     *     label: String, // label of the view
     *     url: String, // url to the provided view; may be prefixed with plugin://
     *     controller: Function || String, // controller reference or name
     *     priority: number// priority of the view (default 0)
     *   }
     * </pre>
     *
     * @param {string} key
     * @param {Object} viewProvider
     */
    this.registerDefaultView = function(key, viewProvider) {
      PluginsProvider.registerPlugin('view', key, viewProvider);
    };

    this.registerView = function(key, viewProvider) {
      PluginsProvider.registerPlugin('view', key, viewProvider);
    };

    this.$get = [ 'Uri', 'Plugins', function(Uri, Plugins) {

      var initialized = false;

      /**
       * Initializes the view map to replace prefixes in templates
       *
       * @param map the plugin map
       * @param app the application to resolve plugin references against
       */
      function initializeViews(map) {
        angular.forEach(map, function(viewProviders) {
          angular.forEach(viewProviders, function(viewProvider) {

            if (viewProvider.url) {
              viewProvider.url = Uri.appUri(viewProvider.url);
            }
          });
        });
      }

      function ensureInitialized() {
        if (!initialized) {
          initializeViews(Plugins.getAllProviders('view'));

          initialized = true;
        }
      }

      var service = {

        getProviders: function(options) {
          ensureInitialized();

          return Plugins.getProviders('view', options);
        },

        getProvider: function(options) {

          var viewProviders = this.getProviders(options);
          return (viewProviders || [])[0];
        }
      };

      return service;
    }];
  }];

  module.provider('Views', ViewsProvider);


  var DataProvider = [ 'PluginsProvider', function(PluginsProvider) {

    /**
     * Registers the given dataProvider for the specified data
     *
     * Data provider is an object like the following:
     *
     * <pre>
     *   {
     *     id: String, // id if the view
     *     controller: Function || Array // controller reference
     *   }
     * </pre>
     *
     * @param {string} key
     * @param {Object} dataProvider
     */
    this.registerData = function(key, dataProvider) {
      PluginsProvider.registerPlugin('data', key, dataProvider);
    };

    this.$get = [ 'Plugins', '$injector', function(Plugins, $injector) {

      var service = {

        getProviders: function(options) {
          return Plugins.getProviders('data', options);
        },

        getProvider: function(options) {
          var dataProviders = this.getProviders(options);
          return (dataProviders || [])[0];
        },

        instantiateProviders : function (key, locals) {
          var dataProviders = this.getProviders({ component: key });

          angular.forEach(dataProviders, function (dataProvider) {
            $injector.instantiate(dataProvider.controller, locals);
          });
        }
      };

      return service;
    }];
  }];

  module.provider('Data', DataProvider);
});
