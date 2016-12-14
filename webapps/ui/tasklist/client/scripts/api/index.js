'use strict';
var angular = require('camunda-commons-ui/vendor/angular'),
    CamSDK = require('camunda-commons-ui/vendor/camunda-bpm-sdk-angular');

var apiModule = angular.module('cam.tasklist.client', []);

apiModule.value('HttpClient', CamSDK.Client);

apiModule.value('CamForm', CamSDK.Form);

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
