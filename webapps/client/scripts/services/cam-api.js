/* global define: false */
define([
  'camunda-bpm-sdk-js'
],
function(CamSDK) {
  'use strict';

  return ['Uri', function(Uri) {

    return new CamSDK.Client({
      apiUri: Uri.appUri('engine://'),
      engine: Uri.appUri(':engine')
    });

  }];

});
