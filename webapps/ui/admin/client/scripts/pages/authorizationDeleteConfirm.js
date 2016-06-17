  'use strict';

  module.exports = [
    '$scope', '$q', '$location', 'Uri', 'Notifications', 'AuthorizationResource', '$modalInstance', 'authorizationToDelete', 'formatPermissions', 'getResource', 'getType',
    function($scope,   $q,   $location,   Uri,   Notifications,   AuthorizationResource,   $modalInstance,   authorizationToDelete,   formatPermissions,   getResource,   getType) {

      var DELETE_SUCCESS = 'SUCCESS';

      $scope.authorizationToDelete = authorizationToDelete;

      $scope.formatPermissions = formatPermissions;
      $scope.getResource = getResource;
      $scope.getType = getType;

      $scope.$on('$routeChangeStart', function() {
        $modalInstance.close($scope.status);
      });

      $scope.close = function(status) {
        $modalInstance.close(status);
      };

      $scope.performDelete = function() {
        AuthorizationResource.delete({ action: authorizationToDelete.id }).$promise.then(function() {
          $scope.status = DELETE_SUCCESS;
        });
      };
    }];
