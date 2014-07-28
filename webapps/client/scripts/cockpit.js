/* global ngDefine: false */

/**
 * Cockpit app
 *
 * TODO:
 * - describe the plugin mechanisms
 *
 * @module cockpit
 */

/**
 * @namespace cam.cockpit
 */
(function(window) {
  'use strict';

  /**
   * @memberof cam
   * @name cockpit
   */
  var cockpitCore = [
    'module:cockpit.services:./services/main',
    'module:cockpit.pages:./pages/main',
    'module:cockpit.directives:./directives/main',
    'module:cockpit.filters:./filters/main',
    'module:cockpit.resources:./resources/main' ];

  var commons = [
    'camunda-commons-ui'
  ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource', 'module:ui.bootstrap:angular-ui' ].concat(commons, cockpitCore, plugins);

  ngDefine('cockpit', dependencies, function(module, $) {

    var ModuleConfig = [ '$routeProvider', 'UriProvider', function($routeProvider, UriProvider) {

      $routeProvider.otherwise({ redirectTo: '/dashboard' });

      function getUri(id) {
        var uri = $('base').attr(id);
        if (!id) {
          throw new Error('Uri base for ' + id + ' could not be resolved');
        }

        return uri;
      }

      UriProvider.replace(':appName', 'cockpit');
      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('adminbase://', getUri('app-root') + '/app/admin/');
      UriProvider.replace('cockpit://', getUri('cockpit-api'));
      UriProvider.replace('admin://', getUri('cockpit-api') + '../admin/');
      UriProvider.replace('plugin://', getUri('cockpit-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [ '$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/\/app\/cockpit\/(\w+)(|\/)/);
        if (match) {
          return match[1];
        } else {
          throw new Error('no process engine selected');
        }
      }]);
    }];

    module.config(ModuleConfig);

    return module;
  });

})(window || this);
