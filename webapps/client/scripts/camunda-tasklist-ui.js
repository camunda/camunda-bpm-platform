define('camunda-tasklist-ui', [
  'camunda-commons-ui',
  'camunda-bpm-sdk-js',
  'angular-data-depend',

  'scripts/config/date',
  'scripts/config/routes',
  'scripts/config/locales',
  'scripts/config/tooltip',
  'scripts/config/uris',

  'scripts/controller/cam-tasklist-app-ctrl',
  'scripts/controller/cam-tasklist-view-ctrl',
  'scripts/services/cam-tasklist-assign-notification',
  'scripts/services/cam-tasklist-configuration',

  'scripts/user/index',
  'scripts/variable/index',
  'scripts/tasklist/index',
  'scripts/task/index',
  'scripts/process/index',
  'scripts/navigation/index',
  'scripts/form/index',
  'scripts/filter/index',
  'scripts/api/index',

  'text!scripts/index.html'
], function() {
  'use strict';
  /**
   * @namespace cam
   */

  /**
   * @module cam.tasklist
   */


  var pluginPackages = window.PLUGIN_PACKAGES || [];
  var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];

  require.config({
    packages: pluginPackages
  });

  var tasklistApp;

  var deps = [
    'camunda-commons-ui'
  ].concat(pluginDependencies.map(function(plugin) {
    return plugin.requirePackageName;
  }));


  function bootstrapApp() {
    var angular = require('angular');
    var $ = angular.element;

    $(document).ready(function() {
      angular.bootstrap(document, [
        'cam.tasklist',
        'cam.embedded.forms',
        'cam.tasklist.custom'
      ]);

      setTimeout(function() {
        var $aufocused = $('[autofocus]');
        if ($aufocused.length) {
          $aufocused[0].focus();
        }
      }, 300);
    });
  }


  function loaded() {
    var angular = require('angular');

    function parseUriConfig() {
      var $baseTag = angular.element('base');
      var config = {};
      var names = ['href', 'app-root', 'admin-api', 'engine-api'];
      for(var i = 0; i < names.length; i++) {
        config[names[i]] = $baseTag.attr(names[i]);
      }
      return config;
    }

    var ngDeps = [
      'cam.commons',
      'pascalprecht.translate',
      'ngRoute',
      'dataDepend',
      require('scripts/user/index').name,
      require('scripts/variable/index').name,
      require('scripts/tasklist/index').name,
      require('scripts/task/index').name,
      require('scripts/process/index').name,
      require('scripts/navigation/index').name,
      require('scripts/form/index').name,
      require('scripts/filter/index').name,
      require('scripts/api/index').name,
    ].concat(pluginDependencies.map(function(el){
      return el.ngModuleName;
    }));

    var uriConfig = parseUriConfig();

    tasklistApp = angular.module('cam.tasklist', ngDeps);

    tasklistApp.factory('assignNotification', require('scripts/services/cam-tasklist-assign-notification'));
    tasklistApp.provider('configuration', require('scripts/services/cam-tasklist-configuration'));

    require('scripts/config/locales')(tasklistApp, uriConfig['app-root']);
    require('scripts/config/uris')(tasklistApp, uriConfig);


    tasklistApp.config(require('scripts/config/routes'));
    tasklistApp.config(require('scripts/config/date'));
    tasklistApp.config(require('scripts/config/tooltip'));

    tasklistApp.controller('camTasklistAppCtrl', require('scripts/controller/cam-tasklist-app-ctrl'));
    tasklistApp.controller('camTasklistViewCtrl', require('scripts/controller/cam-tasklist-view-ctrl'));


    // The `cam.tasklist` AngularJS module is now available but not yet bootstraped,
    // it is the right moment to load plugins
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

        bootstrapApp.apply(this, arguments);
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
        bootstrapApp();
      });
    }
  }


  /* live-reload
  // loads livereload client library (without breaking other scripts execution)
  require(['jquery'], function($) {
    $('body').append('<script src="//' + location.hostname + ':LIVERELOAD_PORT/livereload.js?snipver=1"></script>');
  });
  /* */

  // and load the dependencies
  require(deps, loaded);
});
