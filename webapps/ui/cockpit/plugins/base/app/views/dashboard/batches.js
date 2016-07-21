'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/batches.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'batch',
    label: 'Batches',
    template: template,
    pagePath: '#/batch',
    controller: [
      '$scope',
      'camAPI',
      function(
      $scope,
      camAPI
    ) {
        $scope.countRunning = 0;
        $scope.countAll = 0;
        var batchService = camAPI.resource('batch');
        batchService.count(function(err, count) {
          $scope.countRunning = count || 0;
        });
        var historyService = camAPI.resource('history');
        historyService.batchCount(function(err, count) {
          $scope.countAll = count.count || 0;
        });
      }],

    priority: -5
  });
}];
