'use strict';

define([
  'angular',
  'camunda-bpm-sdk-js'
],
function(
  angular,
  CamSDK
) {
  var apiModule = angular.module('cam.tasklist.client', []);

  apiModule.value('HttpClient', CamSDK.Client);

  apiModule.value('CamForm', CamSDK.Form);

  apiModule.factory('camAPIHttpClient', [
          '$rootScope', '$location', '$translate', 'Notifications',
  function($rootScope, $location, $translate, Notifications) {

    function AngularClient(config) {
      this._wrapped = new CamSDK.Client.HttpClient(config);
    }

    angular.forEach(['post', 'get', 'load', 'put', 'del', 'options', 'head'], function(name) {
      AngularClient.prototype[name] = function(path, options) {
        if (!options.done) {
          return;
        }

        if (!$rootScope.authentication) {
          return options.done(new Error('Not authenticated'));
        }

        var original = options.done;

        options.done = function(err, result) {

          function applyResponse() {
            // in case the session expired
            if (err && err.status === 401) {
              // broadcast that the authentication changed
              $rootScope.$broadcast('authentication.changed', null);
              // set authentication to null
              $rootScope.authentication = null;


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

              // broadcast event that a login is required
              // proceeds a redirect to /login
              $rootScope.$broadcast('authentication.login.required');

              return;
            }

            original(err, result);
          }

          var phase = $rootScope.$$phase;

          if(phase !== '$apply' && phase !== '$digest') {
            $rootScope.$apply(applyResponse);
          }
          else {
            applyResponse();
          }
        };

        this._wrapped[name](path, options);
      };
    });

    angular.forEach(['on', 'once', 'off', 'trigger'], function(name) {
      AngularClient.prototype[name] = function() {
        this._wrapped[name].apply(this, arguments);
      };
    });

    return AngularClient;
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

  return apiModule;
});
