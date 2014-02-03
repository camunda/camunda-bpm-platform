ngDefine('camunda.common.pages', function(module) {

  var ResponseErrorHandlerInitializer = [ 
    '$rootScope', '$location', 'Notifications', 'Authentication',
    function($rootScope, $location, Notifications, Authentication) {

    function addError(error) {
      error.http = true;
      error.exclusive = [ 'http' ];
      
      Notifications.addError(error);
    }

    /**
     * A handler function that handles HTTP error responses, 
     * i.e. 4XX and 5XX responses by redirecting / notifying the user.
     */
    function handleHttpError(event, error) {

      var status = error.status,
          data = error.data;

      switch (status) {
      case 500:
        if (data && data.message) {
          addError({
            status: 'Server Error', 
            message: data.message, 
            exceptionType: data.exceptionType
          });
        } else {
          addError({ 
            status: 'Server Error', 
            message: 'The server reported an internal error. Try to refresh the page or login and out of the application.'
          });
        }
        break;

      case 0:
        addError({ status: 'Request Timeout', message:  'Your request timed out. Try to refresh the page.' });
        break;

      case 401:
        Authentication.clear();

        if ($location.absUrl().indexOf('/setup/#') == -1) {
          addError({ type: 'warning', status: 'Session ended', message: 'Your session timed out or was ended from another browser window. Please signin again.' });

          $location.path('/login');
        } else {
          $location.path('/setup');
        }
        break;

      case 403:
        if (data.type == 'AuthorizationException') {
          addError({ 
            status: 'Access Denied', 
            message: 'You are unauthorized to '
            + data.permissionName.toLowerCase() + ' '
            + data.resourceName.toLowerCase()
            + (data.resourceId ? ' ' + data.resourceId : 's')
            + '.' });
        } else {
          addError({
            status: 'Access Denied', 
            message: 'Executing an action has been denied by the server. Try to refresh the page.'
          });
        }
        break;
      
      case 404:
        addError({ status: 'Not found', message: 'A resource you requested could not be found.' });
        break;
      default:
        addError({ 
          status: 'Communication Error', 
          message: 'The application received an unexpected ' + status + ' response from the server. Try to refresh the page or login and out of the application.' 
        });
      }
    }

    // triggered by httpStatusInterceptor
    $rootScope.$on('httpError', handleHttpError);
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
        $window.location.href = Uri.appUri('app://../' + engine.name + '/');
      }
    });
  }];

  var NavigationController = [ '$scope', '$location', function($scope, $location) {

      $scope.activeClass = function(link) {
        var path = $location.absUrl();      
        return path.indexOf(link) != -1 ? 'active' : '';
      };
  }];

  var AuthenticationController = [
    '$scope', '$window', '$cacheFactory', 'Notifications', 'AuthenticationService', 'Uri',
    function($scope, $window, $cacheFactory, Notifications, AuthenticationService, Uri) {
      
      $scope.logout = function() {
        AuthenticationService.logout().then(function() {
          $cacheFactory.get('$http').removeAll();
          $window.location.href = Uri.appUri('app://#/login');
        });
      };
  }];

  module
    .run(ResponseErrorHandlerInitializer)
    .controller('ProcessEngineSelectionController', ProcessEngineSelectionController)
    .controller('AuthenticationController', AuthenticationController)
    .controller('NavigationController', NavigationController);
});