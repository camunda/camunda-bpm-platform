'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
define([
           'angular', 'camunda-bpm-sdk', 'camunda-bpm-sdk-mock',
], function(angular,   CamSDK,            MockClient) {
  var apiModule = angular.module('cam.tasklist.client', []);

  apiModule.factory('camAPI', [
  function() {
    return new CamSDK({
      appUri:     'engine-rest/engine',
      HttpClient: MockClient
    });
  }]);

  return apiModule;
});
