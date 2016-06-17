'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/userCreate.html', 'utf8');

var Controller = ['$scope', 'page', 'UserResource', 'Notifications', '$location', function($scope, page, UserResource, Notifications, $location) {

  $scope.$root.showBreadcrumbs = true;

  page.titleSet('Create User');

  page.breadcrumbsClear();

  page.breadcrumbsAdd([
    {
      label: 'Users',
      href: '#/users/'
    },
    {
      label: 'Create',
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
      Notifications.addMessage({ type: 'success', status: 'Success', message: 'Created new user '+user.profile.id});
      $location.path('/users');
    },
      function() {
        Notifications.addError({ status: 'Failed', message: 'Failed to create user. Check if it already exists.' });
      });
  };

}];

module.exports = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/user-create', {
    template: template,
    controller: Controller,
    authentication: 'required'
  });
}];
