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

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$scope',
  '$q',
  '$location',
  'Uri',
  'Notifications',
  'camAPI',
  '$uibModalInstance',
  'member',
  'memberId',
  'idList',
  '$translate',
  function(
    $scope,
    $q,
    $location,
    Uri,
    Notifications,
    camAPI,
    $modalInstance,
    member,
    memberId,
    idList,
    $translate
  ) {
    var GroupResource = camAPI.resource('group');

    var BEFORE_CREATE = 'beforeCreate',
      PERFORM_CREATE = 'performCancel',
      CREATE_SUCCESS = 'SUCCESS',
      CREATE_FAILED = 'FAILED',
      LOADING_FAILED = 'loadingFailed';

    $scope.user = member;
    $scope.groupIdList = idList;
    $scope.userId = memberId;

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    var pages = ($scope.pages = {current: 1, size: 50, total: 0});
    var selectedGroups = [];

    GroupResource.count().then(function(res) {
      pages.total = res.count;
    });

    function loadAllGroups() {
      var deferred = $q.defer();

      GroupResource.list(
        angular.extend(
          {},
          {
            maxResults: pages.size,
            firstResult: (pages.current - 1) * pages.size
          },
          sorting
        ),
        function(err, res) {
          if (err === null) {
            deferred.resolve(res);
          } else {
            deferred.reject(err.data);
          }
        }
      );

      return deferred.promise;
    }

    var sorting = ($scope.sorting = {});

    $scope.onSortingChanged = function(_sorting) {
      sorting = $scope.sorting = $scope.sorting || {};
      sorting.sortBy = _sorting.sortBy;
      sorting.sortOrder = _sorting.sortOrder;
      filterGroups();
    };

    $scope.onPaginationChange = function() {
      filterGroups();
    };

    var filterGroups = function() {
      $q.all([loadAllGroups()]).then(
        function(results) {
          var availableGroups = results[0];
          $scope.availableGroups = [];
          angular.forEach(availableGroups, function(group) {
            if ($scope.groupIdList.indexOf(group.id) == -1) {
              if (selectedGroups.indexOf(group.id) !== -1) {
                group.checked = true;
              }
              $scope.availableGroups.push(group);
            }
          });

          $scope.status = BEFORE_CREATE;
        },
        function(error) {
          $scope.status = LOADING_FAILED;
          Notifications.addError({
            status: 'Failed',
            message: $translate.instant('GROUP_MEMBERSHIP_CREATE_LOAD_FAILED', {
              message: error.message
            }),
            exclusive: ['type']
          });
        }
      );
    };
    filterGroups();

    $scope.selectGroup = function(group) {
      var index = selectedGroups.indexOf(group.id);

      if (index === -1) {
        selectedGroups.push(group.id);
      } else {
        selectedGroups.splice(index, 1);
      }
    };

    $scope.createGroupMemberships = function() {
      $scope.status = PERFORM_CREATE;

      var completeCount = 0;
      var deferred = $q.defer();
      angular.forEach(selectedGroups, function(groupId) {
        GroupResource.createMember(
          {id: groupId, userId: $scope.userId},
          function(err) {
            completeCount++;
            if (err === null) {
              if (completeCount == selectedGroups.length) {
                deferred.resolve();
              }
            } else {
              if (completeCount == selectedGroups.length) {
                deferred.reject(err);
              }
            }
          }
        );
      });

      deferred.promise.then(
        function() {
          $scope.status = CREATE_SUCCESS;
        },
        function(error) {
          $scope.status = CREATE_FAILED;
          Notifications.addError({
            status: 'Failed',
            message: $translate.instant(
              'GROUP_MEMBERSHIP_CREATE_CREATE_FAILED',
              {message: error.message}
            ),
            exclusive: ['type']
          });
        }
      );
    };

    $scope.close = function(status) {
      $modalInstance.close(status);
    };
  }
];
