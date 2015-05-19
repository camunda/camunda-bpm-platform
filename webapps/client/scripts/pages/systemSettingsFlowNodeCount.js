define(['text!./systemSettingsFlowNodeCount.html'], function(template) {
  'use strict';

  var Controller = [
   '$scope',
   'MetricsResource',
   '$filter',
  function ($scope, MetricsResource, $filter) {

    var dateFilter = $filter('date');
    var now = new Date();

    $scope.startDate = dateFilter(now, 'yyyy') + '-01-01T00:00:00';
    $scope.endDate = dateFilter(now, 'yyyy') + '-12-31T23:59:59';

    $scope.load = function() {

      var options = {
        'name': 'activity-instance-end',
        'startDate': $scope.startDate,
        'endDate': $scope.endDate
      };

      MetricsResource.sum(options).$promise
        .then(function(response) {
          $scope.activityInstances = response.result;
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

