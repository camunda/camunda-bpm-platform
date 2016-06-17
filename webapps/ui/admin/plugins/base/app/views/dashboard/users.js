'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/users.html', 'utf8');

module.exports = [
  'ViewsProvider',
  function(
  ViewsProvider
) {
    ViewsProvider.registerDefaultView('admin.dashboard.section', {
      id: 'user',
      label: 'Users',
      template: template,
      pagePath: '#/users',
      controller: [
        '$scope',
        'camAPI',
        function(
      $scope,
      camAPI
    ) {
          var service = camAPI.resource('user');

          $scope.access = {};

          service.options(function(err, data) {
            if (err) { throw err; }
            $scope.access = {};

            for (var a in data.links) {
              $scope.access[data.links[a].rel] = true;
            }
          });
        }],
      priority: 0
    });
  }];
