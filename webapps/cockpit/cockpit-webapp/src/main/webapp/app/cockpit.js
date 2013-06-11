(function() {

  var cockpitCore = [
    'module:cockpit.pages:./pages/main',
    'module:cockpit.directives:./directives/main',
    'module:cockpit.filters:./filters/main',
    'module:cockpit.resources:./resources/main',
    'module:cockpit.plugin:cockpit-plugin' ];

  var commons = [
    'module:camunda.common.directives:camunda-common/directives/main',
    'module:camunda.common.extensions:camunda-common/extensions/main',
    'module:camunda.common.services:camunda-common/services/main' ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var dependencies = [ 'jquery', 'module:ng', 'module:ngResource' ].concat(commons, cockpitCore, plugins);

  ngDefine('cockpit', dependencies, function(module, $) {

    var Controller = function ($scope, Errors) {

      $scope.appErrors = function () {
        return Errors.errors;
      };

      $scope.removeError = function (error) {
        Errors.clear(error);
      };

      // needed for form validation
      // DO NOT REMOVE FROM DEFAULT CONTROLLER!
      $scope.errorClass = function(form) {
        return form.$valid || !form.$dirty ? '' : 'error';
      };

    };

    Controller.$inject = ['$scope', 'Errors'];

    var ModuleConfig = function($routeProvider, $httpProvider, UriProvider) {
      $httpProvider.responseInterceptors.push('httpStatusInterceptor');
      $routeProvider.otherwise({ redirectTo: '/dashboard' });

      function getUri(id) {
        var uri = $("base").attr(id);
        if (!id) {
          throw new Error("Uri base for " + id + " could not be resolved");
        }

        return uri;
      }

      UriProvider.replace('app://', getUri("cockpit-base"));
      UriProvider.replace('plugin://', getUri("cockpit-base") + "plugin/");
      UriProvider.replace('engine://', getUri("engine-base"));
    };

    ModuleConfig.$inject = ['$routeProvider', '$httpProvider', 'UriProvider'];

    module
      .config(ModuleConfig)
      .controller('DefaultCtrl', Controller);

    return module;

  });

})(window || this);