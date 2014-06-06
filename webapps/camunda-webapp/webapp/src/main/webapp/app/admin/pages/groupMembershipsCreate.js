/* global ngDefine: false, angular: false */
/* jshint browser: true */
ngDefine('admin.pages', function(module) {
  'use strict';

  module.controller('GroupMembershipDialogController', [
            '$scope', '$q', '$location', 'Uri', 'Notifications', 'GroupMembershipResource', 'GroupResource', '$modalInstance', 'user', 'groupIdList',
    function($scope,   $q,   $location,   Uri,   Notifications,   GroupMembershipResource,   GroupResource,   $modalInstance,   user,   groupIdList) {

    var BEFORE_CREATE = 'beforeCreate',
        PERFORM_CREATE = 'performCancel',
        CREATE_SUCCESS = 'SUCCESS',
        CREATE_FAILED = 'FAILED',
        LOADING_FAILED = 'loadingFailed';

    $scope.user = user;
    $scope.groupIdList = groupIdList;

    $scope.$on('$routeChangeStart', function () {
      $modalInstance.close($scope.status);
    });

    function loadAllGroups () {
      var deferred = $q.defer();

      GroupResource.query().$promise.then(function (response) {
        // deferred.resolve(response.data);
        deferred.resolve(response);
      }, function (error) {
        deferred.reject(error.data);
      });

      return deferred.promise;
    }

    $q.all([ loadAllGroups() ]).then(function(results) {
      var availableGroups = results[0];
      $scope.availableGroups = [];
      angular.forEach(availableGroups, function(group) {
        if($scope.groupIdList.indexOf(group.id) == -1) {
          $scope.availableGroups.push(group);
        }
      });
      $scope.status = BEFORE_CREATE;
    }, function (error) {
      $scope.status = LOADING_FAILED;
      Notifications.addError({'status': 'Failed', 'message': 'Loading of groups failed: ' + error.message, 'exclusive': ['type']});
    });

    $scope.createGroupMemberships = function () {
      $scope.status = PERFORM_CREATE;


      var selectedGroupIds = [];
      angular.forEach($scope.availableGroups, function(group){
        if(group.checked) {
          selectedGroupIds.push(group.id);
        }
      });

      var completeCount = 0;
      var deferred = $q.defer();
      angular.forEach(selectedGroupIds, function(groupId) {

        GroupMembershipResource.create({'groupId': groupId, 'userId': $scope.user.id}).$promise.then(function () {
          completeCount++;
          if(completeCount == selectedGroupIds.length) {
            deferred.resolve();
          }
        }, function () {
          completeCount++;
          if(completeCount == selectedGroupIds.length) {
            deferred.reject();
          }
        });

      });

      deferred.promise.then(function() {
        $scope.status = CREATE_SUCCESS;
      }, function (error) {
        $scope.status = CREATE_FAILED;
        Notifications.addError({'status': 'Failed', 'message': 'Creating group memberships failed: ' + error.message, 'exclusive': ['type']});
      });

    };

    $scope.close = function (status) {
      $modalInstance.close(status);
    };
  }]);

});
