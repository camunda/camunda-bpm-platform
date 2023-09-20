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

var template = require('./groupEdit.html?raw');
var tenantTemplate = require('./create-tenant-group-membership.html?raw');
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
    var GroupResource = camAPI.resource('group'),
      TenantResource = camAPI.resource('tenant'),
      UserResource = camAPI.resource('user');

    var refreshBreadcrumbs = function() {
      pageService.breadcrumbsClear();
      pageService.breadcrumbsAdd({
        label: $translate.instant('GROUP_EDIT_GROUPS'),
        href: '#/groups/'
      });
    };

    $scope.$root.showBreadcrumbs = true;

    pageService.titleSet($translate.instant('GROUP_EDIT_GROUP'));
    refreshBreadcrumbs();

    $scope.group = null;
    $scope.groupName = null;

    $scope.decodedGroupId = unescape(encodeURIComponent($routeParams.groupId));

    $scope.groupUserList = null;
    $scope.tenantList = null;

    var groupUserPages = ($scope.groupUserPages = {
      size: 25,
      total: 0,
      current: 1
    });
    var groupTenantPages = ($scope.groupTenantPages = {
      size: 25,
      total: 0,
      current: 1
    });

    var tenantsSorting = ($scope.tenantsSorting = null);
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

    // update group form /////////////////////////////

    var loadGroup = ($scope.loadGroup = function() {
      $scope.groupLoadingState = 'LOADING';
      GroupResource.get(
        {id: $scope.decodedGroupId},
        function(err, res) {
          $scope.groupLoadingState = 'LOADED';
          $scope.group = res;
          $scope.groupName = res.name ? res.name : res.id;
          $scope.groupCopy = angular.copy(res);

          pageService.titleSet(
            $translate.instant('GROUP_EDIT_GROUP', {group: $scope.groupName})
          );

          refreshBreadcrumbs();
          pageService.breadcrumbsAdd({
            label: $scope.groupName,
            href: '#/groups/' + $scope.group.id
          });
        },
        function() {
          $scope.groupLoadingState = 'ERROR';
        }
      );
    });

    $scope.onTenantsSortingChanged = function(_sorting) {
      tenantsSorting = $scope.tenantsSorting = $scope.tenantsSorting || {};
      tenantsSorting.sortBy = _sorting.sortBy;
      tenantsSorting.sortOrder = _sorting.sortOrder;
      updateGroupTenantView();
    };

    $scope.onUsersSortingInitialized = function(_sorting) {
      usersSorting = _sorting;
    };

    $scope.onUsersSortingChanged = function(_sorting) {
      usersSorting = _sorting;
      updateGroupUserView();
    };

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
          groupUserPages.current = newValue;
          updateGroupUserView();
        }
      }
    );

    $scope.$watch(
      function() {
        return (
          $location.search().tab === 'tenants' &&
          tenantsSorting &&
          parseInt(($location.search() || {}).page || '1')
        );
      },
      function(newValue) {
        if (newValue) {
          groupTenantPages.current = newValue;
          updateGroupTenantView();
        }
      }
    );

    $scope.pageChange = function(page) {
      search.updateSilently({page: !page || page === 1 ? null : page});
    };

    var preparePaging = function(pages) {
      var page = pages.current,
        count = pages.size,
        firstResult = (page - 1) * count;

      return {
        firstResult: firstResult,
        maxResults: count
      };
    };

    $scope.canSortUserEntries = true;
    var updateGroupUserView = function() {
      var pagingParams = preparePaging(groupUserPages);
      var searchParams = {memberOfGroup: $scope.decodedGroupId};

      $scope.userLoadingState = 'LOADING';
      UserResource.list(
        angular.extend({}, searchParams, pagingParams, usersSorting),
        function(err, res) {
          if (err === null) {
            $scope.groupUserList = res;
            $scope.userLoadingState = res.length ? 'LOADED' : 'EMPTY';
          } else {
            // When using LDAP, sorting parameters might not work and throw errors
            // Try again with default sorting
            UserResource.list(angular.extend({}, searchParams, pagingParams))
              .then(function(res) {
                $scope.canSortUserEntries = false;
                $scope.groupUserList = res;
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

      UserResource.count(searchParams, function(err, res) {
        groupUserPages.total = res.count;
      });
    };

    var updateGroupTenantView = ($scope.updateGroupTenantView = function() {
      var pagingParams = preparePaging(groupTenantPages);
      var searchParams = {groupMember: $scope.decodedGroupId};

      $scope.tenantLoadingState = 'LOADING';
      TenantResource.list(
        angular.extend({}, searchParams, pagingParams, tenantsSorting),
        function(err, res) {
          if (err === null) {
            $scope.tenantList = res;

            $scope.idList = [];
            angular.forEach($scope.tenantList, function(tenant) {
              $scope.idList.push(tenant.id);
            });

            $scope.tenantLoadingState = res.length ? 'LOADED' : 'EMPTY';
          } else {
            $scope.tenantLoadingState = 'ERROR';
          }
        }
      );

      TenantResource.count(searchParams, function(err, res) {
        groupTenantPages.total = res.count;
      });
    });

    $scope.removeTenant = function(tenantId) {
      TenantResource.deleteGroupMember(
        {groupId: $scope.decodedGroupId, id: tenantId},
        function(err) {
          if (err === null) {
            Notifications.addMessage({
              type: 'success',
              status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
              message: $translate.instant('GROUP_EDIT_REMOVED_FROM_TENANT', {
                group: $scope.group.id
              })
            });
            updateGroupTenantView();
          } else {
            const {
              response: {
                body: {message}
              }
            } = err;
            Notifications.addError({
              status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
              message: $translate.instant(
                'GROUP_EDIT_REMOVED_FROM_TENANT_FAILED',
                {
                  group: $scope.group.id,
                  message
                }
              )
            });
          }
        }
      );
    };

    $scope.updateGroup = function() {
      GroupResource.update(
        angular.extend({}, {groupId: $scope.decodedGroupId}, $scope.group),
        function(err) {
          if (err === null) {
            Notifications.addMessage({
              type: 'success',
              status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
              message: $translate.instant('GROUP_EDIT_UPDATE_SUCCESS')
            });
            loadGroup();
          } else {
            const {
              response: {
                body: {message}
              }
            } = err;
            Notifications.addError({
              status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
              message: $translate.instant('GROUP_EDIT_UPDATE_FAILED', {message})
            });
          }
        }
      );
    };

    // delete group form /////////////////////////////

    $scope.deleteGroup = function() {
      $modal
        .open({
          template: confirmationTemplate,
          controller: [
            '$scope',
            '$timeout',
            function($dialogScope, $timeout) {
              $dialogScope.question = $translate.instant(
                'GROUP_EDIT_DELETE_CONFIRM',
                {group: $scope.group.id}
              );
              $dialogScope.delete = () => {
                GroupResource.delete({id: $scope.decodedGroupId}, function(
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
                          'GROUP_EDIT_DELETE_SUCCESS',
                          {
                            group: $scope.group.id
                          }
                        )
                      });
                    }, 200);
                    $location.path('/groups');
                    $dialogScope.$close();
                  } else {
                    const {
                      response: {
                        body: {message}
                      }
                    } = err;
                    Notifications.addError({
                      status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                      message: $translate.instant('GROUP_EDIT_DELETE_FAILED', {
                        message
                      })
                    });
                  }
                });
              };
            }
          ]
        })
        .catch(angular.noop);
    };

    // tenant membership dialog /////////////////////////
    var openCreateDialog = function(dialogCfg) {
      var dialog = $modal.open({
        controller: dialogCfg.ctrl,
        template: dialogCfg.template,
        resolve: dialogCfg.resolve
      });

      dialog.result
        .then(function(result) {
          if (result === 'SUCCESS') {
            dialogCfg.callback();
          }
        })
        .catch(angular.noop);
    };

    var prepareResolveObject = function(listObj) {
      return angular.extend(
        {},

        {
          member: function() {
            return $scope.group;
          },
          memberId: function() {
            return $scope.decodedGroupId;
          }
        },

        listObj
      );
    };

    $scope.openCreateTenantMembershipDialog = function() {
      var dialogConfig = {
        ctrl: 'TenantMembershipDialogController',
        template: tenantTemplate,
        callback: updateGroupTenantView,
        resolve: prepareResolveObject({
          idList: function() {
            return $scope.idList;
          }
        })
      };

      openCreateDialog(dialogConfig);
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

    loadGroup();

    if (!$location.search().tab) {
      $location.search({tab: 'group'});
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
    $routeProvider.when('/groups/:groupId', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];
