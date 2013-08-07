ngDefine('camunda.common.pages', function(module) {

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
        if($location.absUrl().indexOf("/setup/#")==-1) {
          $location.path("/login");
        } else {
          $location.path("/setup");
        }
        break;

      case 403:
        if(!!data.type && data.type=="AuthorizationException") {
          Notifications.addError({ status: "Error", message :  "You are unauthorized to "
            + data.permissionName.toLowerCase()+" "
            + data.resourceName.toLowerCase()
            + (!!data.resourceId ? " " + data.resourceId : "s")
            + "." });
          break;
        }
        
      default:
        Notifications.addError({ status: "Error", message :  "A problem occurred: Try to refresh the view or login and out of the application. If the problem persists, contact your administrator." });
      }
    };
  };

  var DefaultController = [ '$scope', '$rootScope', 'Notifications', 'Authentication', '$location', function($scope, $rootScope, Notifications, Authentication, $location) {
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
        $location.path('/')
      }
    });

    $scope.$watch('currentEngine', function(engine) {
      if (engine && current !== engine.name) {
        $window.location.href = Uri.appUri("app://../" + engine.name + "/");
      }
    });
  }];

  var NavigationController = [ '$scope', '$location', function($scope, $location) {

      $scope.activeClass = function(link) {
        var path = $location.absUrl();      
        return path.indexOf(link) != -1 ? "active" : "";
      };
  }];

  var AuthenticationController = [
    '$scope', 'Notifications', 'Authentication', '$location',
    function($scope, Notifications, Authentication, $location) {
      
      $scope.logout = function() {
        Authentication.logout();
        $location.path("#/login");
      }
  }];

  module
    .controller('DefaultController', DefaultController)
    .controller('ProcessEngineSelectionController', ProcessEngineSelectionController)
    .controller('AuthenticationController', AuthenticationController)
    .controller('NavigationController', NavigationController);
});