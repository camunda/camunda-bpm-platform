ngDefine('admin.pages', function(module, $) {

  function CreateGroupMembershipController ($scope, $q, $location, Uri, Notifications, GroupMembershipResource, GroupResource) {

    var BEFORE_CREATE = 'beforeCreate',
        PERFORM_CREATE = 'performCancel',
        CREATE_SUCCESS = 'createSuccess',
        CREATE_FAILED = 'createFailed',
        LOADING_FAILED = 'loadingFailed';

    $scope.$on('$routeChangeStart', function () {
      $scope.close();
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

      var promises = [];

      function createMembership(group, user) {
        var deferred = $q.defer();

        if(group.checked) {             
          GroupMembershipResource.create({'groupId': group.id, 'userId':user.id}).$then(function (response) {
            deferred.resolve(response.data);
          }, function (error) {
            deferred.reject(error.data);
          });
        }
        return deferred.promise;        
      }

      angular.forEach($scope.availableGroups, function(group){
        promises.push(createMembership(group, $scope.user));
      });

      $q.all([ promises ]).then(function(results) {
        $scope.status = CREATE_SUCCESS;        
        $scope.loadGroups();
        Notifications.addMessage({type:"success", status:"Success", message:"User was added to groups."});        
      }, function (error) {
        $scope.status = CREATE_FAILED;
        Notifications.addError({'status': 'Failed', 'message': 'Creating group memberships failed: ' + error.message, 'exclusive': ['type']});
      });

    };

    $scope.close = function (status) {
      $scope.createGroupMembershipDialog.close();
    };
  };

  module.controller('GroupMembershipDialogController', [ '$scope',
                                                         '$q',
                                                         '$location',
                                                         'Uri',
                                                         'Notifications',
                                                         'GroupMembershipResource',
                                                         'GroupResource',
                                                         CreateGroupMembershipController ]);

});