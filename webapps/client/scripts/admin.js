/* global ngDefine: false */

/**
 * @module admin
 *
 */

/**
 * @namespace cam.admin
 */
(function() {
  'use strict';

  /**
   * @memberof cam
   * @name admin
   */
  var adminCore = [
    'module:ngRoute:angular-route',
    'module:admin.pages:./pages/main',
    'module:admin.directives:./directives/main',
    'module:admin.filters:./filters/main',
    'module:admin.services:./services/main',
    'module:admin.resources:./resources/main'
  ];

  var commons = [
    'camunda-commons-ui'
  ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource', 'module:ui.bootstrap:angular-ui' ].concat(commons, adminCore, plugins);

  ngDefine('admin', dependencies, function(module, $) {

    var ModuleConfig = [ '$routeProvider', 'UriProvider', function($routeProvider, UriProvider) {

      $routeProvider.otherwise({ redirectTo: '/users' });

      function getUri(id) {
        var uri = $('base').attr(id);
        if (!id) {
          throw new Error('Uri base for ' + id + ' could not be resolved');
        }

        return uri;
      }

      UriProvider.replace(':appName', 'admin');
      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('cockpitbase://', getUri('app-root') + '/app/cockpit/');
      UriProvider.replace('admin://', getUri('admin-api'));
      UriProvider.replace('plugin://', getUri('admin-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [ '$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/\/app\/admin\/(\w+)(|\/)/);
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
