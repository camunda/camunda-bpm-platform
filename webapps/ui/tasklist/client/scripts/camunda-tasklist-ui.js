'use strict';

var $ = window.jQuery = window.$ = require('jquery');

var commons = require('camunda-commons-ui/lib');
var sdk = require('camunda-commons-ui/vendor/camunda-bpm-sdk-angular');
require('angular-data-depend');

var angular = require('camunda-commons-ui/vendor/angular');
var dataDepend = require('angular-data-depend');
var camCommon = require('../../../common/scripts/module');

  /**
   * @namespace cam
   */

  /**
   * @module cam.tasklist
   */

function bootstrapApp() {
  $(document).ready(function() {
    angular.bootstrap(document.documentElement, [
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

module.exports = function(pluginDependencies) {

  var ngDeps = [
    commons.name,
    'pascalprecht.translate',
    'ngRoute',
    'dataDepend',
    require('./tasklist/index').name,
    require('./task/index').name,
    require('./process/index').name,
    require('./navigation/index').name,
    require('./form/index').name,
    require('./filter/index').name,
    require('./api/index').name,
    require('./shortcuts/plugins/index').name
  ].concat(pluginDependencies.map(function(el) {
    return el.ngModuleName;
  }));

  function parseUriConfig() {
    var $baseTag = $('base');
    var config = {};
    var names = ['href', 'app-root', 'admin-api', 'tasklist-api', 'engine-api'];
    for(var i = 0; i < names.length; i++) {
      config[names[i]] = $baseTag.attr(names[i]);
    }
    return config;
  }

  var uriConfig = parseUriConfig();

  var tasklistApp = angular.module('cam.tasklist', ngDeps);

  tasklistApp.factory('assignNotification', require('./services/cam-tasklist-assign-notification'));
  tasklistApp.provider('configuration', require('./../../../common/scripts/services/cam-configuration')(window.camTasklistConf, 'Tasklist'));

  require('./../../../common/scripts/services/locales')(tasklistApp, uriConfig['app-root'], 'tasklist');
  require('./config/uris')(tasklistApp, uriConfig);


  tasklistApp.config(require('./config/routes'));
  tasklistApp.config(require('./config/date'));
  tasklistApp.config(require('./config/tooltip'));

  tasklistApp.controller('camTasklistAppCtrl', require('./controller/cam-tasklist-app-ctrl'));
  tasklistApp.controller('camTasklistViewCtrl', require('./controller/cam-tasklist-view-ctrl'));

  bootstrapApp();

};

module.exports.exposePackages = function(container) {
  container.angular = angular;
  container.jquery = $;
  container['camunda-commons-ui'] = commons;
  container['camunda-bpm-sdk-js'] = sdk;
  container['angular-data-depend'] = dataDepend;
  container['cam-common'] = camCommon;
};

  /* live-reload
  // loads livereload client library (without breaking other scripts execution)
  $('body').append('<script src="//' + location.hostname + ':LIVERELOAD_PORT/livereload.js?snipver=1"></script>');
  /* */
