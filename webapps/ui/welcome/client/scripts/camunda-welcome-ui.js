'use strict';
/* jshint browserify: true */
var $ = window.jQuery = window.$ = require('jquery');

var commons = require('camunda-commons-ui/lib');
var sdk = require('camunda-commons-ui/vendor/camunda-bpm-sdk-angular');

var APP_NAME = 'cam.welcome';

var angular = require('camunda-commons-ui/vendor/angular');
var pagesModule = require('./pages/main');
var directivesModule = require('./directives/main');
var servicesModule = require('./services/main');

module.exports = function(pluginDependencies) {

  var ngDependencies = [
    'ng',
    'ngResource',
    'pascalprecht.translate',
    commons.name,
    pagesModule.name,
    directivesModule.name,
    servicesModule.name
  ].concat(pluginDependencies.map(function(el) {
    return el.ngModuleName;
  }));

  var appNgModule = angular.module(APP_NAME, ngDependencies);

  function getUri(id) {
    var uri = $('base').attr(id);
    if (!id) {
      throw new Error('Uri base for ' + id + ' could not be resolved');
    }

    return uri;
  }

  var ModuleConfig = [
    '$routeProvider',
    'UriProvider',
    function(
      $routeProvider,
      UriProvider
    ) {
      $routeProvider.otherwise({ redirectTo: '/welcome' });

      UriProvider.replace(':appName', 'welcome');
      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('adminbase://', getUri('app-root') + '/app/admin/');
      UriProvider.replace('welcome://', getUri('welcome-api'));
      UriProvider.replace('admin://', getUri('welcome-api') + '../admin/');
      UriProvider.replace('plugin://', getUri('welcome-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [ '$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/\/app\/welcome\/([\w-]+)(|\/)/);
        if (match) {
          return match[1];
        } else {
          throw new Error('no process engine selected');
        }
      }]);
    }];

  appNgModule.provider('configuration', require('./../../../common/scripts/services/cam-configuration')(window.camWelcomeConf, 'Welcome'));
  appNgModule.controller('WelcomePage', require('./controllers/welcome-page'));

  appNgModule.config(ModuleConfig);

  require('./../../../common/scripts/services/locales')(appNgModule, getUri('app-root'), 'welcome');

  angular.bootstrap(document.documentElement, [ appNgModule.name, 'cam.welcome.custom' ]);

  if (top !== window) {
    window.parent.postMessage({ type: 'loadamd' }, '*');
  }
};

module.exports.exposePackages = function(container) {
  container.angular = angular;
  container.jquery = $;
  container['camunda-commons-ui'] = commons;
  container['camunda-bpm-sdk-js'] = sdk;
  container['cam-common'] = require('../../../common/scripts/module');
};


/* live-reload
// loads livereload client library (without breaking other scripts execution)
$('body').append('<script src="//' + location.hostname + ':LIVERELOAD_PORT/livereload.js?snipver=1"></script>');
/* */
