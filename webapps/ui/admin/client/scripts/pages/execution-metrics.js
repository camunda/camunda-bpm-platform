'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/execution-metrics.html', 'utf8');
var CamSDK = require('camunda-commons-ui/vendor/camunda-bpm-sdk');

var Controller = [
  '$scope',
  'Uri',
  'camAPI',
  function($scope, Uri, camAPI) {

    var MetricsResource = camAPI.resource('metrics');


    var now = new Date();
    $scope.startDate = now.getFullYear() + '-01-01T00:00:00';
    $scope.endDate = now.getFullYear() + '-12-31T23:59:59';
    $scope.loadingState = 'INITIAL';

    $scope.load = function() {
      $scope.loadingState = 'LOADING';
      // promises??? NOPE!
      CamSDK.utils.series({
        flowNodes: function(cb) {
          MetricsResource.sum({
            name: 'activity-instance-start',
            startDate: $scope.startDate,
            endDate: $scope.endDate
          }, function(err, res) {
            cb(err, !err ? res.result : null);
          });
        },
        decisionElements: function(cb) {
          MetricsResource.sum({
            name: 'executed-decision-elements',
            startDate: $scope.startDate,
            endDate: $scope.endDate
          }, function(err, res) {
            cb(err, !err ? res.result : null);
          });
        }
      }, function(err, res) {
        $scope.loadingState = 'LOADED';
        if (err) {
          $scope.loadingState = 'ERROR';
          return;
        }
        $scope.metrics = res;
      });

    };

    $scope.load();

  }];

module.exports = ['ViewsProvider', function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('admin.system', {
    id: 'system-settings-metrics',
    label: 'Execution Metrics',
    template: template,
    controller: Controller,
    priority: 900
  });
}];
