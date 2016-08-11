'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tasks.html', 'utf8');
var series = require('camunda-bpm-sdk-js').utils.series;

module.exports = [
  'ViewsProvider',
  function(
    ViewsProvider
  ) {
    ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
      id: 'tasks',
      label: 'User Tasks',
      template: template,
      pagePath: '#/tasks',
      checkActive: function(path) {
        return path.indexOf('#/tasks') > -1;
      },
      controller: [
        '$scope',
        'camAPI',
        function(
          $scope,
          camAPI
        ) {
          $scope.count = 0;
          $scope.loadingState = 'LOADING';

          $scope.closed = Math.round(Math.random() * 100);

          var HistoryResource = camAPI.resource('history');

          $scope.finishedSearchQuery = function() {
            var searchObject = [{
              type: 'TAfinished',
              operator: 'eq',
              value: '',
              name: ''
            }];

            return encodeURI(JSON.stringify(searchObject));
          };

          series({
            unfinished: function(cb) {
              HistoryResource.taskCount({ unfinished: true }, function(err, data) {
                cb(err, data ? data.count : null);
              });
            },
            finished: function(cb) {
              HistoryResource.taskCount({ finished: true }, function(err, data) {
                cb(err, data ? data.count : null);
              });
            }
          }, function(err, results) {
            if (err) {
              $scope.loadingError = err.message;
              $scope.loadingState = 'ERROR';
              throw err;
            }
            $scope.loadingState = 'LOADED';
            $scope.count = results;
          });
        }],

      priority: 20
    });
  }];
