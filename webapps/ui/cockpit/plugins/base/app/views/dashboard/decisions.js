'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decisions.html', 'utf8');

module.exports = [
  'ViewsProvider',
function (
  ViewsProvider
) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'decisions',
    label: 'Decisions',
    template: template,
    pagePath: '#/decisions',
    checkActive: function (path) {
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
      var service = camAPI.resource('decision-definition');
      service.count({
        latestVersion: true
      }, function (err, count) {
        if (err) {
          $scope.loadingError = err.message;
          $scope.loadingState = 'ERROR';
          throw err;
        }
        $scope.loadingState = 'LOADED';
        $scope.count = count || 0;
      });
    }],

    priority: 0
  });
}];
