ngDefine('admin.pages', function(module, $) {

  function AuthorizationCreateController ($scope, $q, $location, Uri, Notifications, AuthorizationResource) {
    
    $scope.isCreateNewAuthorization = false;

    var newAuthorization = $scope.newAuthorization = {};

    var updatePermissions = function() {                
      $scope.availablePermissions = [];

      for (var i = 0; i < $scope.getPermissionsForResource().length; i++) {
        if($scope.selectedPermissions.indexOf($scope.getPermissionsForResource()[i]) == -1) {
          $scope.availablePermissions.push($scope.getPermissionsForResource()[i]);
        }
      };
    }

    var resetForm = function() {
      newAuthorization = $scope.newAuthorization = {
        type: 1,
        resourceType: Number($scope.selectedResourceType),
        resourceId: "*"
      };

      $scope.selectedPermissions = ["ALL"];

      $scope.identityId =undefined;
      $scope.identityType = 'User';
      updatePermissions();
    }

    resetForm();

    $scope.toggleCreateNewForm = function() {
      $scope.isCreateNewAuthorization = !$scope.isCreateNewAuthorization;
      if(!$scope.isCreateNewAuthorization) {
        resetForm();
      }
    }

    var isIdentityIdDisabled = $scope.isIdentityIdDisabled = function() {
      return newAuthorization.type == 0;
    }

    $scope.setIdentityType = function(identityType) {
      $scope.identityType = identityType;
    }
    

    $scope.addPermission = function(perm) {
      if($scope.selectedPermissions.indexOf("ALL")!= -1
        || $scope.selectedPermissions.indexOf("NONE")!= -1) {
        $scope.selectedPermissions = [];
      }
      $scope.selectedPermissions.push(perm); 
      updatePermissions();           
    }

    $scope.addAllPermissions = function() {
      $scope.selectedPermissions = [ "ALL" ];
      updatePermissions();
    }

    $scope.$watch('newAuthorization.type', function() {
      if(newAuthorization.type == 0) {
        $scope.identityId = '*';
        $scope.identityType = 'User';
      } else {
        $scope.identityId = undefined;
        $scope.identityType = 'Group';
      }
    });

    $scope.createAuthorization = function() {

      newAuthorization.permissions = $scope.selectedPermissions;
     
      if($scope.identityType == 'User') {
        newAuthorization.userId = $scope.identityId; 
      }

      if($scope.identityType == 'Group') {
        newAuthorization.groupId = $scope.identityId; 
      }
      
      AuthorizationResource.create(newAuthorization).$then(function(response) {
        resetForm();
        $scope.loadAuthorizations();
      });      
      
    };

  };

  module.controller('AuthorizationCreateController', [ '$scope',
                                                         '$q',
                                                         '$location',
                                                         'Uri',
                                                         'Notifications',
                                                         'AuthorizationResource',
                                                         AuthorizationCreateController ]);

});