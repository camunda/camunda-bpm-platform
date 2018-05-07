'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/tenantCreate.html', 'utf8');

var Controller = ['$scope', 'page', 'camAPI', 'Notifications', '$location', '$translate', function($scope, page, camAPI, Notifications, $location, $translate) {

  var TenantResource = camAPI.resource('tenant');

  $scope.$root.showBreadcrumbs = true;

  page.titleSet($translate.instant('TENANTS_CREATE_TENANT'));

  page.breadcrumbsClear();

  page.breadcrumbsAdd([
    {
      label: $translate.instant('TENANTS_TENANTS'),
      href: '#/tenants/'
    },
    {
      label: $translate.instant('TENANTS_CREATE_NEW'),
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
          status : $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
          message : $translate.instant('TENANTS_CREATE_TENANT_SUCCESS', { tenant: tenant.id })
        });
        $location.path('/tenants');

      } else {
        Notifications.addError({
          status : $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
          message : $translate.instant('TENANTS_CREATE_TENANT_FAILED')
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
