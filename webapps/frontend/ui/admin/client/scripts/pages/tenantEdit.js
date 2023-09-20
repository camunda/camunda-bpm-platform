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

var template = require('./tenantEdit.html?raw');
var confirmationTemplate = require('./generic-confirmation.html?raw');

var angular = require('camunda-commons-ui/vendor/angular');

var Controller = [
  '$scope',
  'page',
  '$routeParams',
  'search',
  'camAPI',
  'Notifications',
  '$location',
  '$uibModal',
  'unescape',
  '$translate',
  function(
    $scope,
    pageService,
    $routeParams,
    search,
    camAPI,
    Notifications,
    $location,
    $modal,
    unescape,
    $translate
  ) {
    var TenantResource = camAPI.resource('tenant'),
      GroupResource = camAPI.resource('group'),
      UserResource = camAPI.resource('user');

    $scope.$root.showBreadcrumbs = true;

    pageService.titleSet($translate.instant('TENANTS_TENANT'));

    function refreshBreadcrumbs() {
      pageService.breadcrumbsClear();

      pageService.breadcrumbsAdd([
        {
          label: $translate.instant('TENANTS_TENANTS'),
          href: '#/tenants'
        }
      ]);
    }

    refreshBreadcrumbs();

    $scope.tenant = null;
    $scope.tenantName = null;
    $scope.decodedTenantId = unescape(
      encodeURIComponent($routeParams.tenantId)
    );
    $scope.tenantGroupList = null;
    $scope.tenantUserList = null;

    var tenantGroupPages = ($scope.tenantGroupPages = {size: 25, total: 0}),
      tenantUserPages = ($scope.tenantUserPages = {size: 25, total: 0});

    var groupsSorting = null;
    var usersSorting = null;

    // common form validation //////////////////////////

    /** form must be valid & user must have made some changes */
    $scope.canSubmit = function(form, modelObject) {
      return (
        form.$valid &&
        !form.$pristine &&
        (!modelObject ||
          !angular.equals($scope[modelObject], $scope[modelObject + 'Copy']))
      );
    };

    // update tenant form /////////////////////////////

    var loadTenant = ($scope.loadTenant = function() {
      $scope.tenantLoadingState = 'LOADING';

      TenantResource.get(
        $scope.decodedTenantId,
        function(err, res) {
          $scope.tenantLoadingState = 'LOADED';
          $scope.tenant = res;
          $scope.tenantName = res.name ? res.name : res.id;
          $scope.tenantCopy = angular.copy(res);

          pageService.titleSet(
            $translate.instant('TENANTS_TENANT_TITLE', {
              tenant: $scope.tenantName
            })
          );

          refreshBreadcrumbs();

          pageService.breadcrumbsAdd([
            {
              label: $scope.tenantName,
              href: '#/tenants/' + $scope.tenant.id
            }
          ]);
        },
        function() {
          $scope.tenantLoadingState = 'ERROR';
        }
      );
    });

    $scope.onUsersSortingInitialized = function(_sorting) {
      usersSorting = _sorting;
    };

    $scope.onUsersSortingChanged = function(_sorting) {
      usersSorting = _sorting;
      updateTenantUserView();
    };

    // update users list
    $scope.$watch(
      function() {
        return (
          $location.search().tab === 'users' &&
          usersSorting &&
          parseInt(($location.search() || {}).page || '1')
        );
      },
      function(newValue) {
        if (newValue) {
          tenantUserPages.current = newValue;
          updateTenantUserView();
        }
      }
    );

    $scope.onGroupsSortingInitialized = function(_sorting) {
      groupsSorting = _sorting;
    };

    $scope.onGroupsSortingChanged = function(_sorting) {
      groupsSorting = _sorting;
      updateTenantGroupView();
    };

    // update groups list
    $scope.$watch(
      function() {
        return (
          $location.search().tab === 'groups' &&
          groupsSorting &&
          parseInt(($location.search() || {}).page || '1')
        );
      },
      function(newValue) {
        if (newValue) {
          tenantGroupPages.current = newValue;
          updateTenantGroupView();
        }
      }
    );

    $scope.pageChange = function(page) {
      search.updateSilently({page: !page || page === 1 ? null : page});
    };

    function prepareTenantMemberView(memberPages) {
      var page = memberPages.current,
        count = memberPages.size,
        firstResult = (page - 1) * count;

      return {
        searchParams: {
          memberOfTenant: $scope.decodedTenantId
        },
        pagingParams: {
          firstResult: firstResult,
          maxResults: count
        }
      };
    }

    function updateTenantGroupView() {
      var prep = prepareTenantMemberView(tenantGroupPages);

      $scope.groupLoadingState = 'LOADING';
      GroupResource.list(
        angular.extend({}, prep.searchParams, prep.pagingParams, groupsSorting),
        function(err, res) {
          if (err === null) {
            $scope.tenantGroupList = res;
            $scope.groupLoadingState = res.length ? 'LOADED' : 'EMPTY';
          } else {
            $scope.groupLoadingState = 'ERROR';
          }
        }
      );

      GroupResource.count(prep.searchParams, function(err, res) {
        tenantGroupPages.total = res.count;
      });
    }

    $scope.canSortUserEntries = true;

    function updateTenantUserView() {
      var prep = prepareTenantMemberView(tenantUserPages);

      $scope.userLoadingState = 'LOADING';
      UserResource.list(
        angular.extend({}, prep.searchParams, prep.pagingParams, usersSorting),
        function(err, res) {
          if (err === null) {
            $scope.tenantUserList = res;
            $scope.userLoadingState = res.length ? 'LOADED' : 'EMPTY';
          } else {
            // When using LDAP, sorting parameters might not work and throw errors
            // Try again with default sorting
            UserResource.list(
              angular.extend({}, prep.searchParams, prep.pagingParams)
            )
              .then(function(res) {
                $scope.canSortUserEntries = false;
                $scope.tenantUserList = res;
                $scope.userLoadingState = res.length ? 'LOADED' : 'EMPTY';

                Notifications.addMessage({
                  status: $translate.instant('USERS_NO_SORTING_HEADER'),
                  message: $translate.instant('USERS_NO_SORTING_BODY'),
                  exclusive: true
                });
              })
              .catch(function() {
                $scope.userLoadingState = 'ERROR';
              });
          }
        }
      );

      UserResource.count(prep.searchParams, function(err, res) {
        tenantUserPages.total = res.count;
      });
    }

    $scope.updateTenant = function() {
      var updateData = {
        id: $scope.decodedTenantId,
        name: $scope.tenant.name
      };

      TenantResource.update(updateData, function(err) {
        if (err === null) {
          Notifications.addMessage({
            type: 'success',
            status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
            message: $translate.instant('TENANTS_TENANT_UPDATE_SUCCESS')
          });
          loadTenant();
        } else {
          const {
            response: {
              body: {message}
            }
          } = err;
          Notifications.addError({
            status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
            message: $translate.instant('TENANTS_TENANT_UPDATE_FAILED', {
              message
            })
          });
        }
      });
    };

    // delete group form /////////////////////////////

    $scope.deleteTenant = function() {
      $modal.open({
        template: confirmationTemplate,
        controller: [
          '$scope',
          '$timeout',
          function($dialogScope, $timeout) {
            $dialogScope.question = $translate.instant(
              'TENANTS_TENANT_DELETE_CONFIRM',
              {tenant: $scope.tenant.id}
            );
            $dialogScope.delete = () => {
              TenantResource.delete({id: $scope.decodedTenantId}, function(
                err
              ) {
                if (err === null) {
                  $timeout(() => {
                    Notifications.addMessage({
                      type: 'success',
                      status: $translate.instant(
                        'NOTIFICATIONS_STATUS_SUCCESS'
                      ),
                      message: $translate.instant(
                        'TENANTS_TENANT_DELETE_SUCCESS',
                        {
                          tenant: $scope.tenant.id
                        }
                      )
                    });
                  }, 200);
                  $location.path('/tenants');
                  $dialogScope.$close();
                } else {
                  const {
                    response: {
                      body: {message}
                    }
                  } = err;
                  Notifications.addError({
                    status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                    message: $translate.instant(
                      'TENANTS_TENANT_DELETE_FAILED',
                      {
                        tenant: $scope.tenant.id,
                        message
                      }
                    )
                  });
                }
              });
            };
          }
        ]
      });
    };

    // page controls ////////////////////////////////////

    $scope.show = function(fragment) {
      return fragment === $location.search().tab;
    };

    $scope.activeClass = function(link) {
      var path = $location.absUrl();
      return path.indexOf(link) !== -1 ? 'active' : '';
    };

    // initialization ///////////////////////////////////

    loadTenant();

    if (!$location.search().tab) {
      $location.search({tab: 'tenant'});
      $location.replace();
    }

    // translate
    $scope.translate = function(token, object) {
      return $translate.instant(token, object);
    };
  }
];

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/tenants/:tenantId', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];
