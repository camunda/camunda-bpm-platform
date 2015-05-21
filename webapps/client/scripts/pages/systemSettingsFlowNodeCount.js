define([
  'text!./systemSettingsFlowNodeCount.html',
  'camunda-bpm-sdk-js'
], function(
  template,
  CamSDK
) {
  'use strict';

  var Controller = [
   '$scope',
   'Uri',
   '$filter',
  function ($scope, Uri, $filter) {

    var metricsService = new CamSDK.Client({
      apiUri: Uri.appUri('engine://'),
      engine: Uri.appUri(':engine')
    }).resource('metrics');


    var dateFilter = $filter('date');
    var now = new Date();

    $scope.startDate = dateFilter(now, 'yyyy') + '-01-01T00:00:00';
    $scope.endDate = dateFilter(now, 'yyyy') + '-12-31T23:59:59';

    $scope.load = function() {

      var options = {
        'name': 'activity-instance-start',
        'startDate': $scope.startDate,
        'endDate': $scope.endDate
      };

      metricsService.sum(options, function(err, res) {
        $scope.activityInstances = res.result;
        $scope.$apply();
      });

     };

    $scope.load();

  }];

  return ['ViewsProvider', function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('admin.system', {
      id: 'system-settings-flow-node-count',
      label: 'Flow Node Count',
      template: template,
      controller: Controller,
      priority: 900
    });
  }];
});

