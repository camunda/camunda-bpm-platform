ngDefine('camunda.common.pages.login', [ 'angular', 'require', 'module:camunda.common.services.authentication:camunda-common/services/Authentication' ], 
  function(module, angular, require) {

  var Controller = ['$scope', 'Authentication', 'Notifications', '$location',
           function ($scope, Authentication, Notifications, $location) {

    if (Authentication.username()) {
      $location.path("/");
    }

    $scope.login = function () {
      Authentication
        .login($scope.username, $scope.password)
        .then(function(success) {
          Notifications.clearAll();
          
          if (success) {
            $location.path("/");
          } else {
            Notifications.addError({ status: "Login Failed", message: "Username / password are incorrect" });
          }
        });
    }
  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/login', {
      templateUrl: require.toUrl('./login.html'),
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});