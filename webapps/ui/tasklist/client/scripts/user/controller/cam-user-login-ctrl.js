'use strict';

module.exports = [
  '$scope',
  '$translate',
  'AuthenticationService',
  'Notifications',
  function(
    $scope,
    $translate,
    AuthenticationService,
    Notifications
  ) {

    function loginSuccess() {
      $translate('LOGGED_IN').then(function(translated) {
        Notifications.addMessage({
          duration: 5000,
          status: translated,
          exclusive: true
        });
      });
    }

    function loginError() {
      $translate('CREDENTIALS_ERROR').then(function(translated) {
        Notifications.addError({
          status: translated,
          scope: $scope
        });
      });
    }

    $scope.login = function() {
      AuthenticationService
        .login($scope.username, $scope.password)
        .then(loginSuccess, loginError);
    };

  }];
