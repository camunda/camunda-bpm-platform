/* global define, require, console, document, window, setTimeout: false */
'use strict';

define('snap-win', ['snap-svg'], function(snap) {
  window.Snap = snap;
});

define('camunda-tasklist-ui', [
  'camunda-tasklist-ui/require-conf',
  'snap-win'
], function(
  rjsConf
) {
  /**
   * @namespace cam
   */

  /**
   * @module cam.tasklist
   */


  var tasklistApp;


  var appModules = rjsConf.shim['camunda-tasklist-ui'];


  var deps = [
    'angular',
    'text!camunda-tasklist-ui/index.html'
  ].concat(appModules);



  // converts AMD paths to angular module names
  // "camunda-tasklist-ui/filter" will be "cam.tasklist.filter"
  function rj2ngNames(names) {
    var name, translated = [];
    for (var n = 0; n < names.length; n++) {
      if(names[n].indexOf("!") !== -1) continue;
      name = (require(names[n]) || {}).name;
      if (name) translated.push(name);
    }
    return translated;
  }


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
    var $ = angular.element;

    function parseUriConfig() {
      var $baseTag = angular.element('base');
      var config = {};
      var names = ['href', 'app-root', 'admin-api', 'engine-api'];
      for(var i = 0; i < names.length; i++) {
        config[names[i]] = $baseTag.attr(names[i]);
      }
      return config;
    }

    var ngDeps = rj2ngNames(appModules).concat([
      'pascalprecht.translate',
      'ngRoute',
      'dataDepend'
    ]);

    var uriConfig = parseUriConfig();

    tasklistApp = angular.module('cam.tasklist', ngDeps);

    var notificationsPanel = require('camunda-commons-ui/directives/notificationsPanel');
    tasklistApp.directive('notificationsPanel', notificationsPanel);

    var engineSelect = require('camunda-commons-ui/directives/engineSelect');
    tasklistApp.directive('engineSelect', engineSelect);

    var autoFill = require('camunda-commons-ui/directives/autoFill');
    tasklistApp.directive('autoFill', autoFill);

    var nl2br = require('camunda-commons-ui/directives/nl2br');
    tasklistApp.directive('nl2br', nl2br);

    var compileTemplate = require('camunda-commons-ui/directives/compileTemplate');
    tasklistApp.directive('compileTemplate', compileTemplate);

    var camWidgetInlineField = require('camunda-commons-ui/widgets/inline-field/cam-widget-inline-field');
    tasklistApp.directive('camWidgetInlineField', camWidgetInlineField);


    tasklistApp.factory('assignNotification', require('camunda-tasklist-ui/services/cam-tasklist-assign-notification'));
    tasklistApp.provider('configuration', require('camunda-tasklist-ui/services/cam-tasklist-configuration'));

    require('camunda-tasklist-ui/config/locales')(tasklistApp, uriConfig['app-root']);
    require('camunda-tasklist-ui/config/uris')(tasklistApp, uriConfig);


    tasklistApp.config(require('camunda-tasklist-ui/config/routes'));
    tasklistApp.config(require('camunda-tasklist-ui/config/date'));
    tasklistApp.config(require('camunda-tasklist-ui/config/tooltip'));

    tasklistApp.controller('camTasklistAppCtrl', require('camunda-tasklist-ui/controller/cam-tasklist-app-ctrl'));
    tasklistApp.controller('camTasklistViewCtrl', require('camunda-tasklist-ui/controller/cam-tasklist-view-ctrl'));


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
      bootstrapApp();
    }
  }


  // configure require.js
  require.config(rjsConf);

  // and load the dependencies
  require(deps, loaded);
});
