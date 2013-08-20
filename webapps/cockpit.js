(function(window) {

  var cockpitCore = [
    'module:cockpit.pages:./pages/main',
    'module:cockpit.directives:./directives/main',
    'module:cockpit.filters:./filters/main',
    'module:cockpit.services:./services/main',
    'module:cockpit.resources:./resources/main',
    'module:cockpit.plugin:cockpit-plugin' ];

  var commons = [
    'module:camunda.common.directives:camunda-common/directives/main',
    'module:camunda.common.extensions:camunda-common/extensions/main',
    'module:camunda.common.services:camunda-common/services/main',
    'module:camunda.common.pages:camunda-common/pages/main' ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource', 'module:ui.bootstrap:angular-ui' ].concat(commons, cockpitCore, plugins);

  ngDefine('cockpit', dependencies, function(module, $, angular) {

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
      UriProvider.replace('adminbase://', getUri('app-root') + "/app/admin/");
      UriProvider.replace('cockpit://', getUri('cockpit-api'));
      UriProvider.replace('admin://', getUri('cockpit-api') + "../admin/");
      UriProvider.replace('plugin://', getUri('cockpit-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [ '$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/app\/cockpit\/(\w+)\//);
        if (match) {
          return match[1];
        } else {
          throw new Error('no process engine selected');
        }
      }]);
    }];

    module.config(ModuleConfig);
  });

})(window || this);