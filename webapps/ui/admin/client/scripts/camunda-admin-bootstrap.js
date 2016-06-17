window.__define('camunda-admin-bootstrap', [
  './scripts/camunda-admin-ui'
], function() {
  'use strict';

  var camundaAdminUi = window.CamundaAdminUi;

  requirejs.config({
    baseUrl: '../../../lib'
  });

  var requirePackages = window;
  camundaAdminUi.exposePackages(requirePackages);

  window.define = window.__define;
  window.require = window.__require;

  requirejs(['globalize'], function(globalize) {
    globalize(requirejs, ['angular', 'camunda-commons-ui', 'camunda-bpm-sdk-js', 'jquery'], requirePackages);

    var pluginPackages = window.PLUGIN_PACKAGES || [];
    var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];

    pluginPackages.forEach(function(plugin) {
      var node = document.createElement('link');
      node.setAttribute('rel', 'stylesheet');
      node.setAttribute('href', plugin.location + '/plugin.css');
      document.head.appendChild(node);
    });

    requirejs.config({
      packages: pluginPackages,
      baseUrl: '../',
      paths: {
        ngDefine: '../../lib/ngDefine'
      }
    });

    var dependencies = ['angular', 'ngDefine'].concat(pluginDependencies.map(function(plugin) {
      return plugin.requirePackageName;
    }));

    requirejs(dependencies, function(angular) {
      // we now loaded admin and the plugins, great
      // before we start initializing admin though (and leave the requirejs context),
      // lets see if we should load some custom scripts first

      if (typeof window.camAdminConf !== 'undefined' && window.camAdminConf.customScripts) {
        var custom = window.camAdminConf.customScripts || {};

        // copy the relevant RequireJS configuration in a empty object
        // see: http://requirejs.org/docs/api.html#config
        var conf = {};
        [
          'baseUrl',
          'paths',
          'bundles',
          'shim',
          'map',
          'config',
          'packages',
          // 'nodeIdCompat',
          'waitSeconds',
          'context',
          // 'deps', // not relevant in this case
          'callback',
          'enforceDefine',
          'xhtml',
          'urlArgs',
          'scriptType'
          // 'skipDataMain' // not relevant either
        ].forEach(function(prop) {
          if (custom[prop]) {
            conf[prop] = custom[prop];
          }
        });

        // configure RequireJS
        requirejs.config(conf);

        // load the dependencies and bootstrap the AngularJS application
        requirejs(custom.deps || [], function() {

          // create a AngularJS module (with possible AngularJS module dependencies)
          // on which the custom scripts can register their
          // directives, controllers, services and all when loaded
          angular.module('cam.admin.custom', custom.ngDeps);

          window.define = undefined;
          window.require = undefined;

          // now that we loaded the plugins and the additional modules, we can finally
          // initialize Admin
          camundaAdminUi(pluginDependencies);
        });
      }
      else {
        // for consistency, also create a empty module
        angular.module('cam.admin.custom', []);

        // make sure that we are at the end of the require-js callback queue.
        // Why? => the plugins will also execute require(..) which will place new
        // entries into the queue.  if we bootstrap the angular app
        // synchronously, the plugins' require callbacks will not have been
        // executed yet and the angular modules provided by those plugins will
        // not have been defined yet. Placing a new require call here will put
        // the bootstrapping of the angular app at the end of the queue
        require([], function() {
          window.define = undefined;
          window.require = undefined;
          camundaAdminUi(pluginDependencies);
        });
      }
    });

  });

});

requirejs(['camunda-admin-bootstrap'], function() {});
