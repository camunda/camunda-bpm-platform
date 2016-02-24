'use strict';
var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  CamSDK = require('camunda-bpm-sdk-js/lib/angularjs/index');

  var apiModule = angular.module('cam.tasklist.client', []);

  apiModule.value('HttpClient', CamSDK.Client);

  apiModule.value('CamForm', CamSDK.Form);

  apiModule.run([ '$rootScope', 'Notifications', '$translate', function($rootScope, Notifications, $translate) {
    $rootScope.$on('authentication.login.required', function() {
      $translate([
        'SESSION_EXPIRED',
        'SESSION_EXPIRED_MESSAGE'
      ]).then(function(translations) {
        Notifications.addError({
          status: translations.SESSION_EXPIRED,
          message: translations.SESSION_EXPIRED_MESSAGE,
          exclusive: true
        });
      });
    });
  }]);

  apiModule.factory('camAPI', [
          'camAPIHttpClient',
          '$window',
          'Uri',
  function(camAPIHttpClient, $window, Uri) {

    var conf = {
      apiUri:     'engine-rest/api/engine',
      HttpClient: camAPIHttpClient,
      engine: Uri.appUri(':engine')
    };
    if ($window.tasklistConf) {
      for (var c in $window.tasklistConf) {
        conf[c] = $window.tasklistConf[c];
      }
    }

    return new CamSDK.Client(conf);
  }]);

  module.exports = apiModule;
