define('camunda-tasklist-bootstrap', [
  './scripts/camunda-tasklist-ui'
  //'globalize',
  // 'ngDefine',
  // 'angular'
], function (camundaTasklistUi) {
  'use strict';

  require.config({
    baseUrl: '../../../lib'
  });

  require(['globalize'], function(globalize) {
    var requirePackages = {};

    camundaTasklistUi.exposePackages(requirePackages);
    globalize(require, ['angular', 'camunda-commons-ui', 'camunda-bpm-sdk-js'], requirePackages);

    var pluginPackages = window.PLUGIN_PACKAGES || [];
    var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];

    require.config({
      packages: pluginPackages,
      baseUrl: '../',
      paths: {
        ngDefine: '../../lib/ngDefine'
      }
    });

    var dependencies = ['angular', 'ngDefine'].concat(pluginDependencies.map(function(plugin) {
      return plugin.requirePackageName;
    }));

    require(dependencies, function(angular) {

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
        ].forEach(function (prop) {
          if (custom[prop]) {
            conf[prop] = custom[prop];
          }
        });

        // configure RequireJS
        require.config(conf);

        // load the dependencies and bootstrap the AngularJS application
        require(custom.deps || [], function() {

          // create a AngularJS module (with possible AngularJS module dependencies)
          // on which the custom scripts can register their
          // directives, controllers, services and all when loaded
          angular.module('cam.tasklist.custom', custom.ngDeps);

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
          camundaTasklistUi(pluginDependencies);
        });
      }

    });

  });

});

require(['camunda-tasklist-bootstrap'], function(){});
