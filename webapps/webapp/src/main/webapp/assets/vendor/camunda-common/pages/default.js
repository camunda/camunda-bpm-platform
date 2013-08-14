ngDefine('camunda.common.pages', function(module) {

  var ResponseErrorHandler = function(Notifications, Authentication, $location) {

    function clearErrors() {
      Notifications.clear({ type: 'http' });
    }

    function addError(error) {
      error.http = true;

      Notifications.addError(error);
    }

    this.handlerFn = function(event, responseError) {
      var status = responseError.status,
          data = responseError.data;

      clearErrors();

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
        if ($location.absUrl().indexOf('/setup/#')==-1) {
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
    };
  };

  var DefaultController = [ '$scope', '$rootScope', 'Notifications', 'Authentication', '$location', function($scope, $rootScope, Notifications, Authentication, $location) {
    $rootScope.$on('responseError', new ResponseErrorHandler(Notifications, Authentication, $location).handlerFn);

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
    '$scope', '$window', 'Notifications', 'Authentication', 'Uri',
    function($scope, $window, Notifications, Authentication, Uri) {
      
      $scope.logout = function() {
        Authentication.logout().then(function() {
          $window.location.href = Uri.appUri('app://#/login');
        });
      };
  }];

  module
    .controller('DefaultController', DefaultController)
    .controller('ProcessEngineSelectionController', ProcessEngineSelectionController)
    .controller('AuthenticationController', AuthenticationController)
    .controller('NavigationController', NavigationController);
});