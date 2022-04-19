/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var fs = require('fs');

var template = require('./tenantCreate.html')();

var Controller = [
  '$scope',
  'page',
  'camAPI',
  'Notifications',
  '$location',
  '$translate',
  function($scope, page, camAPI, Notifications, $location, $translate) {
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
      id: '',
      name: ''
    };

    $scope.createTenant = function() {
      var tenant = $scope.tenant;

      TenantResource.create(tenant, function(err) {
        if (err === null) {
          Notifications.addMessage({
            type: 'success',
            status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
            message: $translate.instant('TENANTS_CREATE_TENANT_SUCCESS', {
              tenant: tenant.id
            })
          });
          $location.path('/tenants');
        } else {
          Notifications.addError({
            status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
            message: $translate.instant('TENANTS_CREATE_TENANT_FAILED')
          });
        }
      });
    };
  }
];

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/tenant-create', {
      template: template,
      controller: Controller,
      authentication: 'required'
    });
  }
];
