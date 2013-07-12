(function() {

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
    'module:camunda.common.services:camunda-common/services/main' ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource' ].concat(commons, cockpitCore, plugins);

  ngDefine('cockpit', dependencies, function(module, $, angular) {

    var ProcessEngineSelectionController = [
      '$scope', '$rootScope', '$http', '$location', '$window', 'Uri', 'Notifications',
      function($scope, $rootScope, $http, $location, $window, Uri, Notifications) {

      var current = Uri.appUri(':engine');
      var enginesByName = {};

      $http.get(Uri.appUri('engine://engine')).then(function(response) {
        $scope.engines = response.data;

        angular.forEach($scope.engines , function(engine) {
          enginesByName[engine.name] = engine;
        });

        $scope.currentEngine = $rootScope.currentEngine = enginesByName[current];

        if (!$scope.currentEngine) {
          Notifications.addError({ status: 'Not found', message: 'The process engine you are trying to access does not exist' });
          $location.path('/dashboard')
        }
      });

      $scope.$watch('currentEngine', function(engine) {
        if (engine && current !== engine.name) {
          $window.location.href = Uri.appUri("app://../" + engine.name + "/");
        }
      });
    }];

    var ModuleConfig = [ '$routeProvider', '$httpProvider', 'UriProvider', function($routeProvider, $httpProvider, UriProvider) {
      $httpProvider.responseInterceptors.push('httpStatusInterceptor');
      $routeProvider.otherwise({ redirectTo: '/dashboard' });

      function getUri(id) {
        var uri = $('base').attr(id);
        if (!id) {
          throw new Error('Uri base for ' + id + ' could not be resolved');
        }

        return uri;
      }

      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('cockpit://', getUri('cockpit-api'));
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

    module
      .config(ModuleConfig)
      .controller('ProcessEngineSelectionController', ProcessEngineSelectionController);

    return module;

  });

})(window || this);