  'use strict';

  var TasklistApp = (function() {

    function TasklistApp() {

      this.refreshProvider = null;

    }

    return TasklistApp;

  })();

  module.exports = [
    'camAPI',
    'configuration',
    '$window',
    '$interval',
    '$scope',
    function(
    camAPI,
    configuration,
    $window,
    $interval,
    $scope
  ) {

    // create a new tasklistApp
      $scope.tasklistApp = new TasklistApp();
      $scope.appVendor = configuration.getAppVendor();
      $scope.appName = configuration.getAppName();

    // doing so, there's no `{{ appVendor }} {{ appName }}`
    // visible in the title tag as the app loads
      var htmlTitle = document.querySelector('head > title');
      htmlTitle.textContent = $scope.appVendor + ' ' + $scope.appName;

      function getUserProfile(auth) {
        if (!auth || !auth.name) {
          $scope.userFullName = null;
          return;
        }

        var userService = camAPI.resource('user');
        userService.profile(auth.name, function(err, info) {
          if (err) {
            $scope.userFullName = null;
            throw err;
          }
          $scope.userFullName = info.firstName + ' ' + info.lastName;
        });
      }

      $scope.$on('authentication.changed', function(ev, auth) {
        getUserProfile(auth);
      });

      getUserProfile($scope.authentication);

      // app wide refresh event triggering
      var refreshInterval = $interval(function() {
        $scope.$root.$broadcast('refresh');
      }, 10000);

      $scope.$on('$destroy', function() {
        $interval.cancel(refreshInterval);
      });
    }];
