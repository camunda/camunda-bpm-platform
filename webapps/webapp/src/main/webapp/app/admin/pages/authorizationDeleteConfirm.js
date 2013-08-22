ngDefine('admin.pages', function(module, $) {

  function ConfirmDeleteAuthorizationController ($scope, $q, $location, Uri, Notifications, AuthorizationResource, dialog, authorizationToDelete, formatPermissions, getResource, getType) {

    var BEFORE_DELETE = 'beforeDelete',
        PERFORM_DELETE = 'performDelete',
        DELETE_SUCCESS = 'SUCCESS',
        DELETE_FAILED = 'FAILED';

    $scope.authorizationToDelete = authorizationToDelete;

    $scope.formatPermissions = formatPermissions;
    $scope.getResource = getResource;
    $scope.getType = getType;
    
    $scope.$on('$routeChangeStart', function () {
      dialog.close($scope.status);
    });

    $scope.close = function (status) {
      dialog.close(status);
    };

    $scope.performDelete = function () {
      AuthorizationResource.delete({ action: authorizationToDelete.id }).$then(function(response) {
        $scope.status = DELETE_SUCCESS;
      });
    };
  };

  module.controller('ConfirmDeleteAuthorizationController', [ '$scope',
                                                         '$q',
                                                         '$location',
                                                         'Uri',
                                                         'Notifications',
                                                         'AuthorizationResource',
                                                         'dialog',
                                                         'authorizationToDelete',
                                                         'formatPermissions',
                                                         'getResource',
                                                         'getType',
                                                         ConfirmDeleteAuthorizationController ]);

});