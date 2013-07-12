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
    'module:camunda.common.services:camunda-common/services/main' ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource' ].concat(commons, adminCore, plugins);

  ngDefine('admin', dependencies, function(module, $, angular) {

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
          $location.path('/users')
        }
      });

      $scope.$watch('currentEngine', function(engine) {
        if (engine && current !== engine.name) {
          $window.location.href = Uri.appUri("app://../" + engine.name + "/");
        }
      });
    }];

    var NavigationController = [
      '$scope', '$location',
      function($scope, $location) {

        $scope.activeClass = function(link) {
          var path = $location.absUrl();      
          return path.indexOf(link) != -1 ? "active" : "";
        };

    }];

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

      UriProvider.replace('app://', getUri('href'));
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

    module
      .config(ModuleConfig)
      .controller('ProcessEngineSelectionController', ProcessEngineSelectionController)
      .controller('NavigationController', NavigationController);

    return module;

  });

})(window || this);