(function() {

  var adminCore = [
    'module:admin.pages:./pages/main',
    'module:admin.directives:./directives/main',
    'module:admin.filters:./filters/main',
    'module:admin.services:./services/main',
    'module:admin.resources:./resources/main'];

  var commons = [
    'module:camunda.common.directives:camunda-common/directives/main',
    'module:camunda.common.extensions:camunda-common/extensions/main',
    'module:camunda.common.resources:camunda-common/resources/main',
    'module:camunda.common.services:camunda-common/services/main',
    'module:camunda.common.pages:camunda-common/pages/main'];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource', 'module:ngCookies' ].concat(commons, adminCore);

  ngDefine('admin', dependencies, function(module, $, angular) {
  
    var ModuleConfig = [ '$routeProvider', '$httpProvider', 'UriProvider', function($routeProvider, $httpProvider, UriProvider) {
      $httpProvider.responseInterceptors.push('httpStatusInterceptor');
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
      UriProvider.replace('cockpitbase://', getUri('app-root') + "/app/cockpit/");
      UriProvider.replace('admin://', getUri('admin-api'));
      UriProvider.replace('plugin://', getUri('admin-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [ '$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/app\/admin\/(\w+)\//);
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