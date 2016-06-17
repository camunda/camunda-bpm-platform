'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/authorizations.html', 'utf8');

module.exports = [
  'ViewsProvider',
  function(
  ViewsProvider
) {
    ViewsProvider.registerDefaultView('admin.dashboard.section', {
      id: 'authorization',
      label: 'Authorizations',
      template: template,
      pagePath: '#/authorization',
      controller: [function() {}],
      access: [
        'AuthorizationResource',
        function(
      AuthorizationResource
    ) {
          return function(cb) {
            AuthorizationResource.check({
              permissionName: 'ALL',
              resourceName: 'authorization',
              resourceType: 4
            })
        .$promise
        .then(function(response) {
          cb(null, response.authorized);
        })
        .catch(cb)
        ;
          };
        }],
      priority: 0
    });
  }];
