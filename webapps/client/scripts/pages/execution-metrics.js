define([
  'text!./execution-metrics.html',
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
  function ($scope, Uri) {

    var metricsService = new CamSDK.Client({
      apiUri: Uri.appUri('engine://'),
      engine: Uri.appUri(':engine')
    }).resource('metrics');


    var now = new Date();
    $scope.startDate = now.getFullYear() + '-01-01T00:00:00';
    $scope.endDate = now.getFullYear() + '-12-31T23:59:59';
    $scope.loadingState = 'INITIAL';

    $scope.load = function() {
      $scope.loadingState = 'LOADING';
      // promises??? NOPE!
      CamSDK.utils.series({
        flowNodes: function (cb) {
          metricsService.sum({
            name: 'activity-instance-start',
            startDate: $scope.startDate,
            endDate: $scope.endDate
          }, function(err, res) {
            cb(err, !err ? res.result : null);
          });
        },
        decisionElements: function (cb) {
          metricsService.sum({
            name: 'executed-decision-elements',
            startDate: $scope.startDate,
            endDate: $scope.endDate
          }, function(err, res) {
            cb(err, !err ? res.result : null);
          });
        }
      }, function (err, res) {
        $scope.loadingState = 'LOADED';
        if (err) {
          $scope.loadingState = 'ERROR';
          $scope.$apply();
          return;
        }
        $scope.metrics = res;
        $scope.$apply();
      });

     };

    $scope.load();

  }];

  return ['ViewsProvider', function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('admin.system', {
      id: 'system-settings-metrics',
      label: 'Execution Metrics',
      template: template,
      controller: Controller,
      priority: 900
    });
  }];
});

