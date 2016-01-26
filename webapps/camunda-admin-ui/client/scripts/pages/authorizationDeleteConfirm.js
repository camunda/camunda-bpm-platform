/* global define: false */
  'use strict';

  module.exports = [
          '$scope', '$q', '$location', 'Uri', 'Notifications', 'AuthorizationResource', '$modalInstance', 'authorizationToDelete', 'formatPermissions', 'getResource', 'getType',
  function($scope,   $q,   $location,   Uri,   Notifications,   AuthorizationResource,   $modalInstance,   authorizationToDelete,   formatPermissions,   getResource,   getType) {

    var BEFORE_DELETE = 'beforeDelete',
        PERFORM_DELETE = 'performDelete',
        DELETE_SUCCESS = 'SUCCESS',
        DELETE_FAILED = 'FAILED';

    $scope.authorizationToDelete = authorizationToDelete;

    $scope.formatPermissions = formatPermissions;
    $scope.getResource = getResource;
    $scope.getType = getType;

    $scope.$on('$routeChangeStart', function () {
      $modalInstance.close($scope.status);
    });

    $scope.close = function (status) {
      $modalInstance.close(status);
    };

    $scope.performDelete = function () {
      AuthorizationResource.delete({ action: authorizationToDelete.id }).$promise.then(function() {
        $scope.status = DELETE_SUCCESS;
      });
    };
  }];
