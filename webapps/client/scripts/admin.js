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
    'angular-route',
    './pages/main',
    './directives/main',
    './filters/main',
    './services/main',
    './resources/main'
  ];

  var commons = [
    'camunda-commons-ui'
  ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var dependencies = [ 'jquery', 'angular', 'angular-ui' ].concat(commons, adminCore,
                       plugins.map(function(plugin) { return plugin.requirePackageName; }));

  define(dependencies, function($, angular) {

    var ngModule = angular.module('admin', ['ngRoute', 'admin.pages', 'admin.directives', 'admin.filters', 'admin.services', 'admin.resources', 'ng', 'ngResource', 'ui.bootstrap'].concat(plugins.map(function(plugin){
      return plugin.ngModuleName;
    })));

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

    ngModule.config(ModuleConfig);

    return ngModule;
  });

})(window || this);
