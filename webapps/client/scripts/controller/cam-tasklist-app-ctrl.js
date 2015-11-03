define(function() {
  'use strict';

  var TasklistApp = (function() {

    function TasklistApp() {

      this.refreshProvider = null;

    }

    return TasklistApp;

  })();

  return [
    'camAPI',
    '$window',
    '$scope',
  function(
    camAPI,
    $window,
    $scope
  ) {

    // create a new tasklistApp
    $scope.tasklistApp = new TasklistApp();

    function getUserProfile(auth) {
      if (!auth || !auth.name) {
        $scope.userFullName = null;
        return;
      }

      var userService = camAPI.resource('user');
      userService.profile(auth.name, function (err, info) {
        if (err) {
          $scope.userFullName = null;
          throw err;
        }
        $scope.userFullName = info.firstName + ' ' + info.lastName;
      });
    }

    $scope.$on('authentication.changed', function (ev, auth) {
      getUserProfile(auth);
    });

    getUserProfile($scope.authentication);

    $scope.$on('authentication.logout.success', function () {
      $window.location.reload();
    });
  }];

});
