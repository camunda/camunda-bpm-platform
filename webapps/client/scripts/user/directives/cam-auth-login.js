define([
  'angular',
  'text!./cam-auth-login.html',
], function(
  angular,
  template
) {
  'use strict';

  return [
    '$translate',
    'AuthenticationService',
    'Notifications',
  function(
    $translate,
    AuthenticationService,
    Notifications
  ) {

    function loginSuccess() {
      $translate('LOGGED_IN').then(function(translated) {
        Notifications.addMessage({
          duration: 5000,
          status: translated
        });
      });
    }


    function loginError() {
      $translate('CREDENTIALS_ERROR').then(function(translated) {
        Notifications.addError({
          status: translated
        });
      });
    }


    return {
      scope: true,

      template: template,

      link: function(scope) {
        scope.login = function() {
          AuthenticationService
            .login(scope.username, scope.password)
            .then(loginSuccess, loginError);
        };
      }
    };
  }];

});
