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
    'module:camunda.common.services:camunda-common/services/main',
    'module:camunda.common.pages.login:camunda-common/pages/login' ];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource', 'module:ngCookies' ].concat(commons, adminCore);

  ngDefine('admin', dependencies, function(module, $, angular) {
  
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
          if (Authentication.username()) {
            Notifications.addError({ status: "Unauthorized", message:  "Your session has expired. Please login again." });
          } else {
            Notifications.addError({ status: "Unauthorized", message:  "Login is required to access this page." });
          }

          Authentication.clear();
          $location.path("/login");

          break;
        default:
          Notifications.addError({ status: "Error", message :  "A problem occurred: Try to refresh the view or login and out of the application. If the problem persists, contact your administrator." });
        }
      };
    };

    var ProcessEngineSelectionController = [
      '$scope', '$rootScope', '$http', '$location', '$window', 'Uri', 'Notifications',
      function($scope, $rootScope, $http, $location, $window, Uri, Notifications) {

      var current = Uri.appUri(':engine');
      var enginesByName = {};

      $http.get(Uri.appUri('engine://engine/')).then(function(response) {
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
      '$scope', '$location', 'Uri',
      function($scope, $location, Uri) {

        $scope.activeClass = function(link) {
          var path = $location.absUrl();      
          return path.indexOf(link) != -1 ? "active" : "";
        };

        $scope.cockpitLink = Uri.appUri("cockpitbase://"+Uri.appUri(":engine")+"/");
        $scope.taskListLink = Uri.appUri("../../../../tasklist");

    }];

    var AuthenticationController = [
      '$scope', 'Notifications', 'Authentication', '$location', '$rootScope', 'Uri',
      function($scope, Notifications, Authentication, $location, $rootScope, Uri) {
    
        $scope.authentication = $rootScope.authentication = Authentication;
          
        $scope.$on("responseError", new ResponseErrorHandler(Notifications, Authentication, $location).handlerFn);

        $scope.logout = function() {
          Authentication.logout();
          $location.path("/");
        }

        $scope.profileLink = Uri.appUri("app://#/users/"+Authentication.auth.username+"?tab=profile");
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

    module
      .config(ModuleConfig)
      .controller('ProcessEngineSelectionController', ProcessEngineSelectionController)
      .controller('NavigationController', NavigationController)
      .controller('AuthenticationController', AuthenticationController);

    return module;

  });

})(window || this);