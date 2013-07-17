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
    'module:camunda.common.services:camunda-common/services/main',
    'module:camunda.common.pages.login:camunda-common/pages/login' ];

  var plugins = window.PLUGIN_DEPENDENCIES || [];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource', 'module:ngCookies'].concat(commons, cockpitCore, plugins);

  ngDefine('cockpit', dependencies, function(module, $, angular) {

    var ResponseErrorHandler = function(Notifications, Authentication, $location) {

      this.handlerFn = function(event, responseError) {
        var status = responseError.status,
            data = responseError.data;

        Notifications.clear({ type: "error" });

        switch (status) {
        case 500:
          if (data && data.message) {
            Notifications.addError({ status: "Error", message: data.message, exceptionType: data.exceptionType });
          } else {
            Notifications.addError({ status: "Error", message: "A problem occurred: Try to refresh the view or login and out of the application. If the problem persists, contact your administrator." });
          }
          break;
        case 0:
          Notifications.addError({ status: "Request Timeout", message:  "Your request timed out. Try refreshing the page." });
          break;
        case 401:
          Authentication.clear();
          $location.path("/login");

          break;
        default:
          Notifications.addError({ status: "Error", message :  "A problem occurred: Try to refresh the view or login and out of the application. If the problem persists, contact your administrator." });
        }
      };
    };

    var DefaultController = [ '$rootScope', 'Notifications', 'Authentication', '$location', function($rootScope, Notifications, Authentication, $location) {
      $rootScope.$on("responseError", new ResponseErrorHandler(Notifications, Authentication, $location).handlerFn);
    }];

    var ProcessEngineSelectionController = [
      '$scope', '$http', '$location', '$window', 'Uri', 'Notifications',
      function($scope, $http, $location, $window, Uri, Notifications) {

      var current = Uri.appUri(':engine');
      var enginesByName = {};

      $http.get(Uri.appUri('engine://engine/')).then(function(response) {
        $scope.engines = response.data;

        angular.forEach($scope.engines , function(engine) {
          enginesByName[engine.name] = engine;
        });

        $scope.currentEngine = enginesByName[current];

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

    var NavigationController = [
      '$scope', '$location', 'Uri',
      function($scope, $location, Uri) {

        $scope.activeClass = function(link) {
          var path = $location.absUrl();      
          return path.indexOf(link) != -1 ? "active" : "";
        };
    }];

    var AuthenticationController = [
      '$scope', 'Notifications', 'Authentication', '$location', 'Uri',
      function($scope, Notifications, Authentication, $location, Uri) {
    
        $scope.authentication = Authentication;

        $scope.$watch('authentication.auth.username', function(newValue) {
          $scope.userName = newValue;
        });

        $scope.logout = function() {
          Authentication.logout();
          $location.path("/");
        }
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

    module
      .config(ModuleConfig)
      .controller('DefaultController', DefaultController)
      .controller('ProcessEngineSelectionController', ProcessEngineSelectionController)
      .controller('AuthenticationController', AuthenticationController)
      .controller('NavigationController', NavigationController);

    return module;
  });

})(window || this);