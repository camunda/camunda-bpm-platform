'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decisions.html', 'utf8');

var series = require('camunda-bpm-sdk-js').utils.series;

module.exports = [
  'ViewsProvider',
  function(
  ViewsProvider
) {
    ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
      id: 'decisions',
      label: 'Decisions',
      template: template,
      pagePath: '#/decisions',
      checkActive: function(path) {
        return path.indexOf('#/decision') > -1;
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


          var decisionDefinitionService = camAPI.resource('decision-definition');
          var historyService = camAPI.resource('history');
          series({
            definitions: function(cb) {
              decisionDefinitionService.count({
                latestVersion: true
              }, cb);
            },
            instances: function(cb) {
              historyService.decisionInstanceCount({}, function(err, data) {
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

      priority: 80
    });
  }];
