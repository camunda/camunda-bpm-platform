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

var template = require('./userEdit.html?raw');
var groupTemplate = require('./create-group-membership.html?raw');
var tenantTemplate = require('./create-tenant-user-membership.html?raw');
var confirmationTemplate = require('./generic-confirmation.html?raw');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/users/:userId', {
      template: template,
      controller: [
        '$scope',
        'page',
        '$routeParams',
        'camAPI',
        'Notifications',
        '$location',
        '$uibModal',
        'authentication',
        'unescape',
        '$translate',
        'AuthenticationService',
        '$http',
        'Uri',
        function(
          $scope,
          page,
          $routeParams,
          camAPI,
          Notifications,
          $location,
          $modal,
          authentication,
          unescape,
          $translate,
          AuthenticationService,
          $http,
          Uri
        ) {
          var GroupResource = camAPI.resource('group'),
            TenantResource = camAPI.resource('tenant'),
            UserResource = camAPI.resource('user');

          var refreshBreadcrumbs = function() {
            page.breadcrumbsClear();
            page.breadcrumbsAdd({
              label: $translate.instant('USERS_USERS'),
              href: '#/users/'
            });
          };

          $scope.$root.$watch('userFullName', function(name) {
            if (name) {
              $scope.currentUserPassword = $translate.instant(
                'USERS_MY_PASSWORD',
                {name: $scope.$root.userFullName}
              );
            }
          });

          $scope.decodedUserId = unescape(
            encodeURIComponent($routeParams.userId)
          );

          $scope.authenticatedUser = authentication;

          // used to display information about the user
          $scope.profile = null;

          // data model for the profile form (profileCopy is used for dirty checking)
          $scope.profile = null;
          $scope.profileCopy = null;

          // data model for the changePassword form
          $scope.credentials = {
            authenticatedUserPassword: '',
            password: '',
            password2: '',
            valid: true
          };

          // list of the user's groups
          $scope.groupList = null;
          $scope.groupIdList = null;

          // common form validation //////////////////////////

          /** form must be valid & user must have made some changes */
          $scope.canSubmit = function(form, modelObject) {
            return (
              form.$valid &&
              !form.$pristine &&
              $scope.credentials.valid &&
              // TODO: investigate "==" or "==="
              (modelObject == null ||
                !angular.equals(
                  $scope[modelObject],
                  $scope[modelObject + 'Copy']
                ))
            );
          };

          // update profile form /////////////////////////////

          var loadProfile = ($scope.loadProfile = function() {
            UserResource.profile({id: $scope.decodedUserId}, function(
              err,
              res
            ) {
              $scope.user = res;
              $scope.persistedProfile = res;

              $scope.profile = angular.copy(res);
              $scope.profileCopy = angular.copy(res);

              var userFullName = [$scope.user.firstName, $scope.user.lastName]
                .filter(function(v) {
                  return !!v;
                })
                .join(' ');

              page.titleSet(
                $translate.instant('USERS_EDIT_USER', {user: userFullName})
              );

              refreshBreadcrumbs();

              page.breadcrumbsAdd({
                label: userFullName,
                href: '#/users/' + $scope.user.id
              });
            });
          });

          $scope.updateProfile = function() {
            var resourceData = angular.extend(
              {},
              {id: $scope.decodedUserId},
              $scope.profile
            );
            UserResource.updateProfile(resourceData, function(err) {
              if (err === null) {
                $scope.persistedProfile = resourceData;
                Notifications.addMessage({
                  type: 'success',
                  status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
                  message: $translate.instant('USERS_EDIT_SUCCESS_MSN')
                });
                loadProfile();
              } else {
                const {
                  response: {
                    body: {message}
                  }
                } = err;
                Notifications.addError({
                  status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                  message: $translate.instant('USERS_EDIT_FAILED', {message})
                });
              }
            });
          };
          // update password form ////////////////////////////

          var resetCredentials = function(form) {
            $scope.credentials.authenticatedUserPassword = '';
            $scope.credentials.password = '';
            $scope.credentials.password2 = '';

            form.$setPristine();
          };

          $scope.updateCredentials = function(form) {
            var credentialsData = {
              authenticatedUserPassword:
                $scope.credentials.authenticatedUserPassword,
              password: $scope.credentials.password
            };

            var resourceData = angular.extend(
              {},
              {id: $scope.decodedUserId},
              credentialsData
            );

            UserResource.updateCredentials(resourceData, function(err) {
              if (err === null) {
                Notifications.addMessage({
                  type: 'success',
                  status: $translate.instant('NOTIFICATIONS_STATUS_PASSWORD'),
                  message: $translate.instant('USERS_PASSWORD_CHANGED'),
                  duration: 5000,
                  exclusive: true
                });
                resetCredentials(form);
              } else {
                if (err.status === 400) {
                  if ($scope.decodedUserId === $scope.authenticatedUser) {
                    Notifications.addError({
                      status: $translate.instant(
                        'NOTIFICATIONS_STATUS_PASSWORD'
                      ),
                      message: $translate.instant(
                        'USERS_OLD_PASSWORD_NOT_VALID'
                      ),
                      exclusive: true
                    });
                  } else {
                    Notifications.addError({
                      status: $translate.instant(
                        'NOTIFICATIONS_STATUS_PASSWORD'
                      ),
                      message: $translate.instant('USERS_PASSWORD_NOT_VALID'),
                      exclusive: true
                    });
                  }
                } else {
                  const {
                    response: {
                      body: {message}
                    }
                  } = err;
                  Notifications.addError({
                    status: $translate.instant('NOTIFICATIONS_STATUS_PASSWORD'),
                    message: $translate.instant(
                      'USERS_PASSWORD_COULD_NOT_CHANGE',
                      {message}
                    )
                  });
                }
              }
            });
          };

          // delete user form /////////////////////////////

          $scope.deleteUser = function() {
            $modal.open({
              template: confirmationTemplate,
              controller: [
                '$scope',
                '$timeout',
                function($dialogScope, $timeout) {
                  $dialogScope.question = $translate.instant(
                    'USERS_USER_DELETE_CONFIRM',
                    {user: $scope.user.id}
                  );
                  $dialogScope.delete = () => {
                    UserResource.delete({id: $scope.decodedUserId}, function(
                      err
                    ) {
                      if (err === null) {
                        if ($scope.authenticatedUser.name !== $scope.user.id) {
                          $timeout(() => {
                            Notifications.addMessage({
                              type: 'success',
                              status: $translate.instant(
                                'NOTIFICATIONS_STATUS_SUCCESS'
                              ),
                              message: $translate.instant(
                                'USERS_USER_DELETE_SUCCESS',
                                {
                                  user: $scope.user.id
                                }
                              )
                            });
                          }, 200);
                          $location.path('/users');
                        } else {
                          $http
                            .get(Uri.appUri('engine://engine/'))
                            .then(function(response) {
                              var engines = response.data;

                              engines.forEach(function(engine) {
                                AuthenticationService.logout(engine.name);
                              });
                            })
                            .catch(angular.noop);
                        }
                        $dialogScope.$close();
                      } else {
                        const {
                          response: {
                            body: {message}
                          }
                        } = err;
                        Notifications.addError({
                          status: $translate.instant(
                            'NOTIFICATIONS_STATUS_ERROR'
                          ),
                          message: $translate.instant(
                            'USERS_USER_DELETE_FAILED',
                            {
                              user: $scope.user.id,
                              message
                            }
                          )
                        });
                      }
                    });
                  };
                  $timeout(() =>
                    Notifications.addMessage({
                      type: 'info',
                      status: $translate.instant('USERS_WARNING'),
                      unsafe: true,
                      message: $translate.instant('USERS_USER_DELETE_INFO')
                    })
                  );
                }
              ]
            });
          };

          // Unlock User
          $scope.unlockUser = function() {
            UserResource.unlock({id: $scope.decodedUserId})
              .then(function() {
                Notifications.addMessage({
                  type: 'success',
                  status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
                  message: $translate.instant('USERS_USER_UNLOCK_SUCCESS', {
                    user: $scope.user.id
                  })
                });
                $location.path('/users');
              })
              .catch(function(e) {
                Notifications.addError({
                  status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                  message: $translate.instant(e.message)
                });
              });
          };

          // group form /////////////////////////////

          var groupsSorting = ($scope.groupsSorting = {});

          $scope.onGroupsSortingChanged = function(_sorting) {
            groupsSorting = $scope.groupsSorting = $scope.groupsSorting || {};
            groupsSorting.sortBy = _sorting.sortBy;
            groupsSorting.sortOrder = _sorting.sortOrder;
            loadGroups();
          };

          $scope.$watch(
            function() {
              return $location.search().tab === 'groups';
            },
            function(newValue) {
              if (newValue)
                GroupResource.count({
                  member: $scope.decodedUserId
                }).then(function(res) {
                  pages.total = res.count;
                });
            }
          );

          var pages = ($scope.pages = {size: 50, total: 0, current: 1});

          $scope.onPaginationChange = function(evt) {
            $scope.pages.current = evt.current;
            loadGroups();
          };

          var loadGroups = ($scope.loadGroups = function() {
            $scope.groupLoadingState = 'LOADING';
            GroupResource.list(
              angular.extend(
                {},
                {
                  member: $scope.decodedUserId,
                  firstResult: (pages.current - 1) * pages.size,
                  maxResults: pages.size
                },
                groupsSorting
              ),
              function(err, res) {
                $scope.groupLoadingState = res.length ? 'LOADED' : 'EMPTY';

                $scope.groupList = res;
                $scope.groupIdList = [];
                angular.forEach($scope.groupList, function(group) {
                  $scope.groupIdList.push(group.id);
                });
              }
            );
          });

          $scope.removeGroup = function(groupId) {
            GroupResource.deleteMember(
              {userId: $scope.decodedUserId, id: groupId},
              function(err) {
                if (err === null) {
                  Notifications.addMessage({
                    type: 'success',
                    status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
                    message: $translate.instant(
                      'USERS_USER_DELETE_FROM_GROUP',
                      {
                        user: $scope.user.id
                      }
                    )
                  });
                  loadGroups();
                } else {
                  const {
                    response: {
                      body: {message}
                    }
                  } = err;
                  Notifications.addError({
                    status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                    message: $translate.instant(
                      'USERS_USER_DELETE_FROM_GROUP_FAILED',
                      {
                        user: $scope.user.id,
                        message
                      }
                    )
                  });
                }
              }
            );
          };

          $scope.openCreateGroupMembershipDialog = function() {
            var dialogConfig = {
              ctrl: 'GroupMembershipDialogController',
              template: groupTemplate,
              callback: $scope.loadGroups,
              resolve: prepareResolveObject({
                idList: function() {
                  return $scope.groupIdList;
                }
              })
            };

            openCreateDialog(dialogConfig);
          };

          // Tenant Form ///////////////////////////////////////////

          var tenantsSorting = ($scope.tenantsSorting = {});

          $scope.onTenantsSortingChanged = function(_sorting) {
            tenantsSorting = $scope.tenantsSorting =
              $scope.tenantsSorting || {};
            tenantsSorting.sortBy = _sorting.sortBy;
            tenantsSorting.sortOrder = _sorting.sortOrder;

            loadTenants();
          };

          $scope.$watch(
            function() {
              return $location.search().tab === 'tenants' && tenantsSorting;
            },
            function(newValue) {
              return newValue && (loadTenants() || countTenants());
            }
          );

          var tenantPages = ($scope.tenantPages = {
            size: 50,
            total: 0,
            current: 1
          });

          var countTenants = function() {
            TenantResource.count({
              userMember: $scope.decodedUserId
            }).then(function(res) {
              tenantPages.total = res.count;
            });
          };

          var loadTenants = ($scope.loadTenants = function() {
            $scope.tenantLoadingState = 'LOADING';
            TenantResource.list(
              angular.extend(
                {},
                {
                  userMember: $scope.decodedUserId,
                  maxResults: tenantPages.size,
                  firstResult: (tenantPages.current - 1) * tenantPages.size
                },
                tenantsSorting
              ),
              function(err, res) {
                $scope.tenantLoadingState = res.length ? 'LOADED' : 'EMPTY';

                $scope.tenantList = res;
                $scope.idList = [];
                angular.forEach($scope.tenantList, function(tenant) {
                  $scope.idList.push(tenant.id);
                });
              }
            );
          });

          $scope.removeTenant = function(tenantId) {
            TenantResource.deleteUserMember(
              {userId: $scope.decodedUserId, id: tenantId},
              function(err) {
                if (err === null) {
                  Notifications.addMessage({
                    type: 'success',
                    status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
                    message: $translate.instant(
                      'USERS_USER_DELETE_FROM_TENANT',
                      {
                        user: $scope.user.id
                      }
                    )
                  });
                  loadTenants();
                } else {
                  const {
                    response: {
                      body: {message}
                    }
                  } = err;
                  Notifications.addError({
                    status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
                    message: $translate.instant(
                      'USERS_USER_DELETE_FROM_TENANT_FAILED',
                      {
                        user: $scope.user.id,
                        message
                      }
                    )
                  });
                }
              }
            );
          };

          $scope.openCreateTenantMembershipDialog = function() {
            var dialogConfig = {
              ctrl: 'TenantMembershipDialogController',
              template: tenantTemplate,
              callback: $scope.loadTenants,
              resolve: prepareResolveObject({
                idList: function() {
                  return $scope.idList;
                }
              })
            };

            openCreateDialog(dialogConfig);
          };

          // Modal Dialog Configuration ///////////////////////////////

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
                  return $scope.user;
                },
                memberId: function() {
                  return $scope.decodedUserId;
                }
              },

              listObj
            );
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

          $scope.$root.showBreadcrumbs = true;

          page.titleSet($translate.instant('USERS_EDIT_USER'));
          refreshBreadcrumbs();

          loadProfile();

          if (!$location.search().tab) {
            $location.search({tab: 'profile'});
            $location.replace();
          }

          // translate
          $scope.translate = function(token, object) {
            return $translate.instant(token, object);
          };
        }
      ],
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];
