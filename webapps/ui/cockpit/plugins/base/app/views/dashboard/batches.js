'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/batches.html', 'utf8');

module.exports = [ 'ViewsProvider', function (ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'batches',
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
      var batchService = camAPI.resource('batch');
      batchService.count(function (err, count) {
        if (err) {
          $scope.countRunning = 'unknown';
          throw err;
        }
        $scope.countRunning = count || 0;
      });
      var historyService = camAPI.resource('history');
      historyService.batchCount(function (err, count) {
        if (err) {
          $scope.countAll = 'unknown';
          throw err;
        }
        $scope.countAll = count.count || 0;
      });
    }],

    priority: 0
  });
}];
