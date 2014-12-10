/**
 * Cockpit app
 *
 * TODO:
 * - describe the plugin mechanisms
 *
 * @module cam.cockpit
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
    './services/main',
    './pages/main',
    './directives/main',
    './filters/main',
    './resources/main' ];

  var commons = [
    'camunda-commons-ui'
  ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var otherDependencies = [ 'jquery', 'angular', 'angular-ui'];

  var dependencies = otherDependencies.concat(commons, cockpitCore);
  define('camunda-cockpit-ui', dependencies, function($, angular) {

    var moduleDependencies = [];
    for(var i = otherDependencies.length; i < arguments.length; i++) {
      moduleDependencies.push(arguments[i].name);
    }
    var cockpitModule = angular.module("cam.cockpit", ['ng', 'ngResource', 'ui.bootstrap'].concat(moduleDependencies, plugins.map(function(el){ return el.ngModuleName; })));

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

    cockpitModule.config(ModuleConfig);

    return cockpitModule;
  });

})(window || this);
