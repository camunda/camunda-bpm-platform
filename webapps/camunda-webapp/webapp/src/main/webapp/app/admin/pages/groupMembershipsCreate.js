ngDefine('admin.pages', function(module, $) {

  function CreateGroupMembershipController ($scope, $q, $location, Uri, Notifications, GroupMembershipResource, GroupResource, dialog, user, groupIdList) {

    var BEFORE_CREATE = 'beforeCreate',
        PERFORM_CREATE = 'performCancel',
        CREATE_SUCCESS = 'SUCCESS',
        CREATE_FAILED = 'FAILED',
        LOADING_FAILED = 'loadingFailed';

    $scope.user = user;
    $scope.groupIdList = groupIdList;

    $scope.$on('$routeChangeStart', function () {
      dialog.close($scope.status);
    });

    function loadAllGroups () {
      var deferred = $q.defer();

      GroupResource.query().$then(function (response) {
        deferred.resolve(response.data);
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
        
        GroupMembershipResource.create({'groupId': groupId, 'userId': $scope.user.id}).$then(function (response) {          
          completeCount++;
          if(completeCount == selectedGroupIds.length) {
            deferred.resolve();
          }
        }, function (error) {
          completeCount++;
          if(completeCount == selectedGroupIds.length) {
            deferred.reject();
          }
        });

      });
     
      deferred.promise.then(function(results) {
        $scope.status = CREATE_SUCCESS;
      }, function (error) {
        $scope.status = CREATE_FAILED;
        Notifications.addError({'status': 'Failed', 'message': 'Creating group memberships failed: ' + error.message, 'exclusive': ['type']});
      });

    };

    $scope.close = function (status) {
      dialog.close(status);
    };
  };

  module.controller('GroupMembershipDialogController', [ '$scope',
                                                         '$q',
                                                         '$location',
                                                         'Uri',
                                                         'Notifications',
                                                         'GroupMembershipResource',
                                                         'GroupResource',
                                                         'dialog',
                                                         'user',
                                                         'groupIdList',
                                                         CreateGroupMembershipController ]);

});