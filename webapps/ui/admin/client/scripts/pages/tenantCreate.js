'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tenantCreate.html', 'utf8');

var Controller = ['$scope', 'page', 'camAPI', 'Notifications', '$location', function($scope, page, camAPI, Notifications, $location) {

  var TenantResource = camAPI.resource('tenant');

  $scope.$root.showBreadcrumbs = true;

  page.titleSet('Create Tenant');

  page.breadcrumbsClear();

  page.breadcrumbsAdd([
    {
      label: 'Tenants',
      href: '#/tenants/'
    },
    {
      label: 'Create New Tenant',
      href: '#/tenants-create'
    }
  ]);

  // data model for tenant
  $scope.tenant = {
    id : '',
    name : ''
  };

  $scope.createTenant = function() {
    var tenant = $scope.tenant;

    TenantResource.create(tenant, function(err) {
      if( err === null ) {
        Notifications.addMessage({
          type : 'success',
          status : 'Success',
          message : 'Created new tenant ' + tenant.id
        });
        $location.path('/tenants');

      } else {
        Notifications.addError({
          status : 'Failed',
          message : 'Failed to create tenant. Check if it already exists.'
        });
      }
    });
  };

}];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/tenant-create', {
    template: template,
    controller: Controller,
    authentication: 'required'
  });
}];
