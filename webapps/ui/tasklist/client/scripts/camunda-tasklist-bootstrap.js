var $ = window.jQuery = window.$ = require('jquery');

window.__define('camunda-tasklist-bootstrap', [
  './scripts/camunda-tasklist-ui'
], function() {
  'use strict';

  function parseUriConfig() {
    var $baseTag = $('base');
    var config = {};
    var names = ['href', 'app-root', 'admin-api', 'tasklist-api', 'engine-api'];
    for(var i = 0; i < names.length; i++) {
      config[names[i]] = $baseTag.attr(names[i]);
    }
    return config;
  }

  var camundaTasklistUi = window.CamundaTasklistUi;

  requirejs.config({
    baseUrl: '../../../lib'
  });
  var requirePackages = window;

  camundaTasklistUi.exposePackages(requirePackages);

  window.define = window.__define;
  window.require = window.__require;

  requirejs(['globalize'], function(globalize) {
    globalize(requirejs, ['angular', 'camunda-commons-ui', 'camunda-bpm-sdk-js', 'jquery', 'angular-data-depend'], requirePackages);

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
      // we now loaded the tasklist and the plugins, great
      // before we start initializing the tasklist though (and leave the requirejs context),
      // lets see if we should load some custom scripts first

      if (typeof window.camTasklistConf !== 'undefined' && window.camTasklistConf.customScripts) {
        var custom = window.camTasklistConf.customScripts || {};

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
          angular.module('cam.tasklist.custom', custom.ngDeps);

          window.define = undefined;
          window.require = undefined;

          // now that we loaded the plugins and the additional modules, we can finally
          // initialize the tasklist
          camundaTasklistUi(pluginDependencies);
        });
      }
      else {
        // for consistency, also create a empty module
        angular.module('cam.tasklist.custom', []);

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
          camundaTasklistUi(pluginDependencies);
        });
      }

    });

  });

  var uriConfig = parseUriConfig();

  if (typeof window.tasklistConf !== 'undefined' && window.tasklistConf.polyfills) {
    var polyfills = window.tasklistConf.polyfills;

    if (polyfills.indexOf('placeholder') > -1) {
      var load = window.requirejs;
      var appRoot = uriConfig['app-root'];

      load([
        appRoot + '/app/tasklist/scripts/placeholders.utils.js',
        appRoot + '/app/tasklist/scripts/placeholders.main.js'
      ], function() {
        load([
          appRoot + '/app/tasklist/scripts/placeholders.jquery.js'
        ], function() {});
      });
    }
  }

});

requirejs(['camunda-tasklist-bootstrap'], function() {});
