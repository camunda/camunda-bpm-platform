'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tenants.html', 'utf8');

module.exports = [
  'ViewsProvider',
  function(
  ViewsProvider
) {
    ViewsProvider.registerDefaultView('admin.dashboard.section', {
      id: 'tenants',
      label: 'Tenants',
      template: template,
      pagePath: '#/tenants',
      controller: [
        '$scope',
        'camAPI',
        function(
      $scope,
      camAPI
    ) {
          var service = camAPI.resource('tenant');

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
