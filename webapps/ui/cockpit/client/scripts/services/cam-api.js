'use strict';

var CamSDK = require('camunda-bpm-sdk-js/lib/angularjs/index');

  module.exports = ['Uri', 'camAPIHttpClient', function(Uri, camAPIHttpClient) {

    return new CamSDK.Client({
      HttpClient: camAPIHttpClient,
      apiUri: Uri.appUri('engine://'),
      engine: Uri.appUri(':engine')
    });

  }];
