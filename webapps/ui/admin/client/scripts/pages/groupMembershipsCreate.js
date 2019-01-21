'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$scope', '$q', '$location', 'Uri', 'Notifications', 'camAPI', '$uibModalInstance', 'member', 'memberId', 'idList', '$translate',
  function($scope,   $q,   $location,   Uri,   Notifications,   camAPI,   $modalInstance,   member,   memberId,   idList, $translate) {

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

    function loadAllGroups() {
      var deferred = $q.defer();

      GroupResource.list(function(err, res) {
        if( err === null ) {
          deferred.resolve(res);
        } else {
          deferred.reject(err.data);
        }
      });

      return deferred.promise;
    }

    var sorting = $scope.sorting = null;

    $scope.onSortingChanged = function(_sorting) {
      sorting = $scope.sorting = $scope.sorting || {};
      sorting.sortBy = _sorting.sortBy;
      sorting.sortOrder = _sorting.sortOrder;
      sorting.sortReverse = _sorting.sortOrder !== 'asc';
    };


    $q.all([ loadAllGroups() ]).then(function(results) {
      var availableGroups = results[0];
      $scope.availableGroups = [];
      angular.forEach(availableGroups, function(group) {
        if($scope.groupIdList.indexOf(group.id) == -1) {
          $scope.availableGroups.push(group);
        }
      });
      $scope.status = BEFORE_CREATE;
    }, function(error) {
      $scope.status = LOADING_FAILED;
      Notifications.addError({
        'status': 'Failed',
        'message': $translate.instant('GROUP_MEMBERSHIP_CREATE_LOAD_FAILED', {message: error.message}),
        'exclusive': ['type']
      });
    });

    $scope.createGroupMemberships = function() {
      $scope.status = PERFORM_CREATE;

      var selectedGroupIds = [];
      angular.forEach($scope.availableGroups, function(group) {
        if(group.checked) {
          selectedGroupIds.push(group.id);
        }
      });

      var completeCount = 0;
      var deferred = $q.defer();
      angular.forEach(selectedGroupIds, function(groupId) {
        GroupResource.createMember({ id: groupId, userId: $scope.userId }, function(err) {
          completeCount++;
          if( err === null ) {
            if(completeCount == selectedGroupIds.length) {
              deferred.resolve();
            }
          } else {
            if( completeCount == selectedGroupIds.length ) {
              deferred.reject(err);
            }
          }
        });

      });

      deferred.promise.then(function() {
        $scope.status = CREATE_SUCCESS;
      }, function(error) {
        $scope.status = CREATE_FAILED;
        Notifications.addError({
          'status': 'Failed',
          'message': $translate.instant('GROUP_MEMBERSHIP_CREATE_CREATE_FAILED', {message: error.message }),
          'exclusive': ['type']
        });
      });

    };

    $scope.close = function(status) {
      $modalInstance.close(status);
    };
  }];
