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
  '$translate',
  function($scope, $q, $location, Uri, Notifications, camAPI, $translate) {
    var AuthorizationResource = camAPI.resource('authorization');

    $scope.addNewAuthorization = function() {
      $scope.authorizations.push({
        inUpdate: true,
        type: '1',
        resourceType: Number($scope.selectedResourceType),
        resourceId: '*',
        permissions: ['ALL'],
        identityId: '',
        identityType: 'Group'
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
      authorization.permissions = ['ALL'];
    };

    $scope.addNonePermissionsTo = function(authorization) {
      authorization.permissions = ['NONE'];
    };

    $scope.availablePermissionsFor = function() {
      return $scope.getPermissionsForResource().filter(function(permission) {
        return permission !== 'ALL' && permission !== 'NONE';
      });
    };

    $scope.changePermissionOf = function(perm, authorization) {
      if (
        authorization.permissions.indexOf(perm) > -1 ||
        authorization.permissions.indexOf('ALL') > -1
      ) {
        $scope.removePermissionFrom(perm, authorization);
      } else {
        $scope.addPermissionTo(perm, authorization);
      }
    };

    $scope.removePermissionFrom = function(perm, authorization) {
      // Remove 'ALL' permission when removing permissions
      if (authorization.permissions.indexOf('ALL') != -1) {
        authorization.permissions = $scope.getPermissionsForResource();
      }

      authorization.permissions = authorization.permissions.filter(function(
        permission
      ) {
        return permission !== perm;
      });

      // Add 'NONE' permission when removing last permission
      if (authorization.permissions.length === 0) {
        authorization.permissions.push('NONE');
      }
    };

    $scope.addPermissionTo = function(perm, authorization) {
      // Remove 'NONE' permission when adding first permission
      if (authorization.permissions.indexOf('NONE') != -1) {
        authorization.permissions = [];
      }

      authorization.permissions.push(perm);

      // Add 'ALL' permission when everything is checked
      if (
        authorization.permissions.length ===
        $scope.getPermissionsForResource().length
      ) {
        authorization.permissions = ['ALL'];
      }
    };

    $scope.confirmUpdateAuthorization = function(authorization) {
      delete authorization.inUpdate;
      delete authorization.groupId;
      delete authorization.userId;

      authorization[
        authorization.identityType === 'Group' ? 'groupId' : 'userId'
      ] = authorization.identityId;

      // create the update query
      var query = {
        permissions: authorization.permissions,
        resourceType: authorization.resourceType,
        resourceId: authorization.resourceId,
        type: authorization.type
      };
      query[authorization.identityType === 'Group' ? 'groupId' : 'userId'] =
        authorization.identityId;
      if (authorization.id) {
        query.id = authorization.id;
      }

      delete authorization.identityId;
      delete authorization.identityType;

      AuthorizationResource.save(query, function(err, result) {
        if (err) {
          Notifications.addError({
            status: query.id
              ? $translate.instant('AUTHORIZATION_UPDATE')
              : $translate.instant('AUTHORIZATION_CREATE'),
            message: err.toString()
          });
          $scope.cancelUpdateAuthorization(authorization);
          var phase = $scope.$root.$$phase;
          if (phase !== '$apply' && phase !== '$digest') {
            $scope.$apply();
          }
        }
        if (result) {
          authorization.id = result.id;
        }
      });
    };

    $scope.cancelUpdateAuthorization = function(authorization) {
      if (!authorization.id) {
        $scope.authorizations.splice(
          $scope.authorizations.indexOf(authorization),
          1
        );
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
      if (authorization.type === '0') {
        authorization.identityId = '*';
        authorization.identityType = 'User';
      }
    };
  }
];
