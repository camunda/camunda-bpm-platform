define([
  'text!./decision-list.html',
  'camunda-bpm-sdk-js'
], function(
  template,
  CamSDK
) {
  'use strict';

  return [ 'ViewsProvider', function (ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'decision-list',
      label: 'Deployed Decisions',
      template: template,
      controller: [
              '$scope', 'Uri',
      function($scope,   Uri) {

        var client = new CamSDK.Client({
          apiUri: Uri.appUri('engine://'),
          engine: Uri.appUri(':engine')
        });

        var decisionDefinitionService = client.resource('decision-definition');

        // get ALL the decisions
        decisionDefinitionService.list({}, function(err, data) {
          $scope.decisionCount = data.length;
          $scope.decisions = data;
        });

      }],

      priority: 0
    });
  }];
});
