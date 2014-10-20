define([
], function(
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

    AuthenticationService
      .logout()
      .then(function() {
        $translate('LOGGED_OUT').then(function(translated) {
          Notifications.add({
            status: translated,
            exclusive: true
          });
        });
      });

  }];

});
