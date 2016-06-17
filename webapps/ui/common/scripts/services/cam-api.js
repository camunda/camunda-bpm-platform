'use strict';

var CamSDK = require('camunda-commons-ui/vendor/camunda-bpm-sdk-angular');

module.exports = ['Uri', 'camAPIHttpClient', function(Uri, camAPIHttpClient) {

  return new CamSDK.Client({
    HttpClient: camAPIHttpClient,
    apiUri: Uri.appUri('engine://'),
    engine: Uri.appUri(':engine')
  });

}];
