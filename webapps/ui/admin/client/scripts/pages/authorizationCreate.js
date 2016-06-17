'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = ['$scope', '$q', '$location', 'Uri', 'Notifications', 'camAPI', function AuthorizationCreateController($scope, $q, $location, Uri, Notifications, camAPI) {

  var AuthorizationResource = camAPI.resource('authorization');

  $scope.addNewAuthorization = function() {
    $scope.authorizations.push({
      inUpdate: true,
      type: 1,
      resourceType: Number($scope.selectedResourceType),
      resourceId: '*',
      permissions: ['ALL'],
      identityId: '',
      identityType: 'User'
    });
  };

  $scope.updateAuthorization = function(authorization) {
    authorization.original = angular.copy(authorization);
    authorization.inUpdate = true;
    authorization.identityId = $scope.getIdentityId(authorization);
    authorization.identityType = authorization.userId ? 'User' : 'Group';
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
      permissions: authorization.permissions,
      resourceType: authorization.resourceType,
      resourceId: authorization.resourceId,
      type: authorization.type
    };
    query[authorization.identityType === 'Group' ? 'groupId' : 'userId'] = authorization.identityId;
    if(authorization.id) {
      query.id = authorization.id;
    }

    delete authorization.identityId;
    delete authorization.identityType;

    AuthorizationResource.save(query, function(err, result) {
      if(err) {
        Notifications.addError({
          status: 'Could not ' + (query.id ? 'update' : 'create') + ' authorization',
          message: err.toString()
        });
        $scope.cancelUpdateAuthorization(authorization);
        $scope.$apply();
      }
      if(result) {
        authorization.id = result.id;
      }
    });

  };

  $scope.cancelUpdateAuthorization = function(authorization) {
    if(!authorization.id) {
      $scope.authorizations.splice($scope.authorizations.indexOf(authorization), 1);
    } else {
      delete authorization.userId;
      delete authorization.groupId;

      angular.forEach(authorization.original, function(value, key) {
        authorization[key] = value;
      });

      delete authorization.original;
      delete authorization.inUpdate;
    }
  };

  $scope.isAuthorizationValid = function(authorization) {
    return !!authorization.identityId && !!authorization.resourceId;
  };

  $scope.isIdentityIdDisabledFor = function(authorization) {
    return authorization.type === '0';
  };

  $scope.ensureValidUser = function(authorization) {
    if(authorization.type === '0') {
      authorization.identityId = '*';
      authorization.identityType = 'User';
    }
  };
}];
