'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$scope', '$q', '$location', 'search', 'Uri', 'Notifications', 'camAPI', '$modalInstance', 'member', 'memberId', 'idList',
  function($scope,   $q,   $location,   search,   Uri,   Notifications,   camAPI,   $modalInstance,   member,   memberId,   idList) {

    var modalPages = $scope.modalPages = {
      size: 20,
      total: 0,
      current: 1
    };

    var checkedItems = $scope.checkedItems = [];
    $scope.checkedItemsCount = 0;

    var addItemPage = function(page) {
      if(checkedItems[page] === undefined) {
        checkedItems[page] = [];
      }
    };

    var addItem = function(id, page) {
      if(checkedItems[page].indexOf(id) === -1) {
        checkedItems[page].push(id);
        $scope.checkedItemsCount++;
      }
    };

    var removeItem = function(id, page) {
      var idx = checkedItems[page].indexOf(id);
      if(idx !== -1) {
        checkedItems[page].splice(idx, 1);
        $scope.checkedItemsCount--;
      }
    };

    var executeItem = function(id, page, callback) {
      if(checkedItems[page].indexOf(id) !== -1) {
        callback();
      }
    };

    $scope.$watch(function() {
      return parseInt(($location.search() || {}).modalPage || '1');
    }, function(newValue) {
      modalPages.current = newValue;

      // Invalidates available tenants when we change the page
      // prevents duplication of tenants in checkedItems
      $scope.availableTenants = undefined;

      addItemPage(modalPages.current);
      loadAllTenants();
    });

    $scope.pageChange = function(page) {
      search.updateSilently({ modalPage: !page || page == 1 ? null : page });
    };

    var TenantResource = camAPI.resource('tenant');

    var BEFORE_CREATE = 'beforeCreate',
        PERFORM_CREATE = 'performCancel',
        CREATE_SUCCESS = 'SUCCESS',
        CREATE_FAILED = 'FAILED',
        LOADING_FAILED = 'loadingFailed';

    $scope.member = member;
    $scope.idList = idList;
    $scope.memberId = memberId;

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.close($scope.status);
    });

    function loadAllTenants() {
      var page = modalPages.current,
          count = modalPages.size,
          firstResult = (page - 1) * count;

      var pagingParams = {
        firstResult: firstResult,
        maxResults: count
      };

      TenantResource.list(pagingParams, function(err, res) {
        if(err === null) {
          $scope.availableTenants = [];
          angular.forEach(res, function(tenant) {
            var id = tenant.id;
            if($scope.idList.indexOf(id) == -1) {

              executeItem(id, modalPages.current, function() {
                tenant.checked = true;
              });

              $scope.availableTenants.push(tenant);
            }
          });
          $scope.status = BEFORE_CREATE;
        } else {
          $scope.status = LOADING_FAILED;
          Notifications.addError({'status': 'Failed', 'message': 'Loading of tenants failed: ' + err.data.message, 'exclusive': ['type']});
        }
      });

      TenantResource.count(function(err, res) {
        if(err === null) {
          modalPages.total = res.count;
        }
      });
    }

    var allTenantsChecked = $scope.allTenantsChecked = function() {
      if($scope.availableTenants !== undefined) {
        var counter = 0;

        angular.forEach($scope.availableTenants, function(tenant) {
          var id = tenant.id;
          if( tenant.checked) {
            counter++;
            addItem(id, modalPages.current);
          } else {
            removeItem(id, modalPages.current);
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
      angular.forEach(checkedItems, function(item) {
        angular.forEach(item, function(id) {
          if(selectedTenantIds.indexOf(id) === -1) {
            selectedTenantIds.push(id);
          }
        });
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
            deferred.resolve(res);
          }

        } else {
          if( completeCount === selectedTenantIds.length ) {
            deferred.reject(err);
          }
        }
      };

      angular.forEach(selectedTenantIds, function(tenantId) {
        createMembershipObj.id = tenantId
                                    .replace(/\//g, '%2F')
                                    .replace(/\\/g, '%5C');

        if( typeof createMembershipObj.userId === 'string' ) {
          TenantResource.createUserMember(createMembershipObj, cb);
        } else {
          TenantResource.createGroupMember(createMembershipObj, cb);
        }

        createMembershipNotification(deferred);
      });
    };

    $scope.createUserMemberships = function() {
      var memberObj = { userId: $scope.memberId };
      createTenantMemberships(memberObj);
    };

    $scope.createGroupMemberships = function() {
      var memberObj = { groupId: $scope.memberId };
      createTenantMemberships(memberObj);
    };

    $scope.close = function(status) {
      $modalInstance.close(status);
    };

    loadAllTenants();
  }];
