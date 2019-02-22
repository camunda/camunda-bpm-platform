'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/userCreate.html', 'utf8');

var Controller = ['$scope', 'page', 'UserResource', 'Notifications', '$location', '$translate', function($scope, page, UserResource, Notifications, $location, $translate) {

  $scope.$root.showBreadcrumbs = true;

  page.titleSet($translate.instant('USERS_CREATE_USER'));

  page.breadcrumbsClear();

  page.breadcrumbsAdd([
    {
      label: $translate.instant('USERS_USERS'),
      href: '#/users/'
    },
    {
      label: $translate.instant('USERS_CREATE'),
      href: '#/users-create'
    }
  ]);

    // data model for user profile
  $scope.profile = {
    id : '',
    firstName : '',
    lastName : '',
    email : ''
  };

    // data model for credentials
  $scope.credentials = {
    password : '',
    password2 : ''
  };

  $scope.createUser = function() {
    var user = {
      profile : $scope.profile,
      credentials : { password : $scope.credentials.password }
    };

    UserResource.createUser(user).$promise.then(function() {
      Notifications.addMessage({ type: 'success', status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'), message: $translate.instant('USERS_CREATE_SUCCESS', {user: user.profile.id})});
      $location.path('/users');
    },
      function(err) {
        Notifications.addError({ status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'), message: $translate.instant('USERS_CREATE_FAILED', {message: err.data.message}) });
      }
      );
  };

}];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/user-create', {
    template: template,
    controller: Controller,
    authentication: 'required'
  });
}];
