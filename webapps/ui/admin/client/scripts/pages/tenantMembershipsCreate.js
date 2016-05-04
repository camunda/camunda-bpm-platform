'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
          '$scope', '$q', '$location', 'Uri', 'Notifications', 'camAPI', '$modalInstance', 'member', 'memberId', 'idList',
  function($scope,   $q,   $location,   Uri,   Notifications,   camAPI,   $modalInstance,   member,   memberId,   idList) {

    var TenantResource = camAPI.resource('tenant');

    var BEFORE_CREATE = 'beforeCreate',
        PERFORM_CREATE = 'performCancel',
        CREATE_SUCCESS = 'SUCCESS',
        CREATE_FAILED = 'FAILED',
        LOADING_FAILED = 'loadingFailed';

    $scope.member = member;
    $scope.idList = idList;
    $scope.memberId = memberId;

    $scope.$on('$routeChangeStart', function () {
      $modalInstance.close($scope.status);
    });

    function loadAllTenants() {
      var deferred = $q.defer();

      TenantResource.list(function(err, res) {
        if(err === null) {
          deferred.resolve(res);
        } else {
          deferred.reject(err.data);
        }
      });

      return deferred.promise;
    }

    $q.all([ loadAllTenants() ]).then(function(results) {
      var availableTenants = results[0];
      $scope.availableTenants = [];
      angular.forEach(availableTenants, function(tenant) {
        if($scope.idList.indexOf(tenant.id) == -1) {
          $scope.availableTenants.push(tenant);
        }
      });
      $scope.status = BEFORE_CREATE;
    }, function (error) {
      $scope.status = LOADING_FAILED;
      Notifications.addError({'status': 'Failed', 'message': 'Loading of tenants failed: ' + error.message, 'exclusive': ['type']});
    });

    
    var allTenantsChecked = $scope.allTenantsChecked = function() {
      if($scope.availableTenants !== undefined) {
        var counter = 0;
        angular.forEach($scope.availableTenants, function(tenant) {
          if( tenant.checked ) {
            counter++;
          }
        });

        return counter === $scope.availableTenants.length;
      }

      return false;
    };

    $scope.checkAllTenants = function() {
      if(allTenantsChecked()) {
        angular.forEach($scope.availableTenants, function(tenant) {
          tenant.checked = false;
        });
      } else {
        angular.forEach($scope.availableTenants, function(tenant) {
          tenant.checked = true;
        });
      }
    };
    
    var prepareMembership = function() {
      $scope.status = PERFORM_CREATE;

      var selectedTenantIds = [];
      angular.forEach($scope.availableTenants, function(tenant){
        if(tenant.checked) {
          selectedTenantIds.push(tenant.id);
        }
      });


      return selectedTenantIds;
    };

    var createMembershipNotification = function(deferred) {
      deferred.promise.then(function() {
        $scope.status = CREATE_SUCCESS;

      }, function(error) {
        $scope.status = CREATE_FAILED;
        Notifications.addError({
          'status': 'Failed',
          'message': 'Creating tenant memberships failed: ' + error.message,
          'exclusive': ['type']
        });
      });
    };

    var createTenantMemberships = function(createMembershipObj) {

      var deferred = $q.defer();
      
      var selectedTenantIds = prepareMembership();

      var completeCount = 0;
      var cb = function(err, res) {
        completeCount++;

        if( err === null ) {
          if( completeCount === selectedTenantIds.length ) {
            deferred.resolve();
          }

        } else {
          if( completeCount === selectedTenantIds.length ) {
            deferred.reject();
          }
        }
      };
      
      angular.forEach(selectedTenantIds, function(tenantId) {
        createMembershipObj.id = tenantId
                                    .replace(/\//g, '%2F')
                                    .replace(/\\/g, '%5C');

        if( typeof createMembershipObj.userId === 'string' ) {
          TenantResource.createUserMember(createMembershipObj, cb)
        } else {
          TenantResource.createGroupMember(createMembershipObj, cb)
        }

        createMembershipNotification(deferred);
      });
    };

    $scope.createUserMemberships = function() {
      var memberObj = { userId: $scope.memberId };
      createTenantMemberships(memberObj)
    };

    $scope.createGroupMemberships = function() {
      var memberObj = { groupId: $scope.memberId };
      createTenantMemberships(memberObj)
    };

    $scope.close = function (status) {
      $modalInstance.close(status);
    };
  }];
