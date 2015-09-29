/* global define: false */
define([
  'camunda-bpm-sdk-js'
],
function(CamSDK) {
  'use strict';

  return ['Uri', 'camAPIHttpClient', function(Uri, camAPIHttpClient) {

    return new CamSDK.Client({
      HttpClient: camAPIHttpClient,
      apiUri: Uri.appUri('engine://'),
      engine: Uri.appUri(':engine')
    });

  }];

});
