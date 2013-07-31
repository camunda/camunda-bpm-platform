ngDefine('admin.pages', function(module, $) {

  function ConfirmDeleteAuthorizationController ($scope, $q, $location, Uri, Notifications, AuthorizationResource) {

    var BEFORE_DELETE = 'beforeDelete',
        PERFORM_DELETE = 'performDelete',
        DELETE_SUCCESS = 'deleteSuccess',
        DELETE_FAILED = 'deleteFailed';

    $scope.$on('$routeChangeStart', function () {
      $scope.close();
    });

    $scope.close = function (status) {
      $scope.confirmDeleteAuthorizationDialog.close();
    };

    $scope.performDelete = function () {
      AuthorizationResource.delete({action: $scope.authorizationToDelete.id}).$then(function(response) {
        $scope.loadAuthorizations();
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
                                                         ConfirmDeleteAuthorizationController ]);

});