'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
define([
           'angular', 'camunda-bpm-sdk', 'camunda-bpm-sdk-mock',
], function(angular,   CamSDK,            MockClient) {
  var apiModule = angular.module('cam.tasklist.client', []);

  apiModule.value('HttpClient', MockClient);

  apiModule.factory('camAPIHttpClient', [ 'HttpClient', '$rootScope', function(HttpClient, $rootScope) {

    function AngularClient(config) {
      this._wrapped = new HttpClient(config);
    }

    angular.forEach('post get put del'.split(' '), function(name) {

      AngularClient.prototype[name] = function(path, options) {
        var original = options.done;

        if (original) {
          options.done = function(err, result) {
            $rootScope.$apply(function() {
              original(err, result);
            });
          };
        }

        this._wrapped[name](path, options);
      };
    });

    return AngularClient;
  }]);

  apiModule.factory('camAPI', [ 'camAPIHttpClient', function(camAPIHttpClient) {

    return new CamSDK({
      appUri:     'engine-rest/engine',
      HttpClient: camAPIHttpClient
    });
  }]);

  return apiModule;
});
