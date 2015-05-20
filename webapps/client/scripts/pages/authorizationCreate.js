define(['camunda-bpm-sdk-js', 'angular'], function(CamSDK, angular) {
  'use strict';
  return [ '$scope', '$q', '$location', 'Uri', 'Notifications', 'AuthorizationResource', function AuthorizationCreateController ($scope, $q, $location, Uri, Notifications, AuthorizationResource) {

    var authorizationService = new CamSDK.Client({
      apiUri: Uri.appUri('engine://'),
      engine: Uri.appUri(':engine')
    }).resource('authorization');

    $scope.isCreateNewAuthorization = false;

    var newAuthorization = $scope.newAuthorization = {};

    var updatePermissions = function() {
      $scope.availablePermissions = [];
      var resourcePermissions = $scope.getPermissionsForResource();

      for (var i = 0; i < resourcePermissions.length; i++) {
        if($scope.selectedPermissions.indexOf(resourcePermissions[i]) < 0) {
          $scope.availablePermissions.push(resourcePermissions[i]);
        }
      }
    };

    var resetForm = function() {
      newAuthorization = $scope.newAuthorization = {
        type: 1,
        resourceType: Number($scope.selectedResourceType),
        resourceId: '*'
      };

      $scope.selectedPermissions = ['ALL'];

      newAuthorization.identityId = undefined;
      $scope.identityType = 'User';
      updatePermissions();
    };

    resetForm();

    $scope.updateAuthorization = function(authorization) {
      authorization.original = angular.copy(authorization);
      authorization.inUpdate = true;
      authorization.identityId = $scope.getIdentityId(authorization);
      authorization.identityType = !!authorization.userId ? 'User' : 'Group';
    };

    $scope.setIdentityTypeFor = function(identityType, authorization) {
      authorization.identityType = identityType;
    };

    $scope.getIdentityTypeFor = function(authorization) {
      return authorization.identityType;
    };

    $scope.addAllPermissionsTo = function(authorization) {
      authorization.permissions = [ 'ALL' ];
    };

    $scope.availablePermissionsFor = function(authorization) {
      var availablePermissions = [];
      var resourcePermissions = $scope.getPermissionsForResource();

      for (var i = 0; i < resourcePermissions.length; i++) {
        if(authorization.permissions.indexOf(resourcePermissions[i]) < 0) {
          availablePermissions.push(resourcePermissions[i]);
        }
      }

      return availablePermissions;
    };

    $scope.addPermissionTo = function(perm, authorization) {
      if(authorization.permissions.indexOf('ALL')!= -1 ||
         authorization.permissions.indexOf('NONE')!= -1) {
        authorization.permissions = [];
      }
      authorization.permissions.push(perm);
    };

    $scope.confirmUpdateAuthorization = function(authorization) {
      delete authorization.inUpdate;
      delete authorization.groupId;
      delete authorization.userId;

      authorization[authorization.identityType === 'Group' ? 'groupId' : 'userId'] = authorization.identityId;

      // create the update query
      var query = {
        id: authorization.id,
        permissions: authorization.permissions,
        resourceType: authorization.resourceType,
        resourceId: authorization.resourceId
      };
      query[authorization.identityType === 'Group' ? 'groupId' : 'userId'] = authorization.identityId;

      delete authorization.identityId;
      delete authorization.identityType;

      authorizationService.update(query, function(result) {
        if(!!result) {
          Notifications.addError({
            status: 'Could not update authorization',
            message: result.toString()
          });
          $scope.cancelUpdateAuthorization(authorization);
          $scope.$apply();
        }
      });

    };

    $scope.cancelUpdateAuthorization = function(authorization) {
      delete authorization.userId;
      delete authorization.groupId;

      angular.forEach(authorization.original, function(value, key) {
        authorization[key] = value;
      });

      delete authorization.original;
      delete authorization.inUpdate;
    };

    $scope.isAuthorizationValid = function(authorization) {
      return !!authorization.identityId && !!authorization.resourceId;
    };


    $scope.toggleCreateNewForm = function() {
      $scope.isCreateNewAuthorization = !$scope.isCreateNewAuthorization;
      if(!$scope.isCreateNewAuthorization) {
        resetForm();
      }
    };

    $scope.isIdentityIdDisabled = function() {
      return newAuthorization.type === 0;
    };

    $scope.setIdentityType = function(identityType) {
      $scope.identityType = identityType;
    };


    $scope.addPermission = function(perm) {
      if($scope.selectedPermissions.indexOf('ALL')!= -1 ||
         $scope.selectedPermissions.indexOf('NONE')!= -1) {
        $scope.selectedPermissions = [];
      }
      $scope.selectedPermissions.push(perm);
      updatePermissions();
    };

    $scope.addAllPermissions = function() {
      $scope.selectedPermissions = [ 'ALL' ];
      updatePermissions();
    };

    $scope.$watch('newAuthorization.type', function() {
      if(newAuthorization.type === 0) {
        newAuthorization.identityId = '*';
        $scope.identityType = 'User';
      } else {
        newAuthorization.identityId = undefined;
        $scope.identityType = 'Group';
      }
    });

    $scope.createAuthorization = function() {

      newAuthorization.permissions = $scope.selectedPermissions;

      if($scope.identityType == 'User') {
        newAuthorization.userId = newAuthorization.identityId;
      }

      if($scope.identityType == 'Group') {
        newAuthorization.groupId = newAuthorization.identityId;
      }

      delete newAuthorization.identityId;

      AuthorizationResource.create(newAuthorization).$promise.then(function(response) {
        resetForm();
        $scope.loadAuthorizations();
      });

    };

  }];
});
