'use strict';
/* jshint browserify: true */
var fs = require('fs');
var template = fs.readFileSync(__dirname + '/user-profile.html', 'utf8');
var angular = require('camunda-commons-ui/vendor/angular');

module.exports = ['camAPI', 'Notifications', function(camAPI, Notifications) {
  return {
    restrict: 'A',

    template: template,

    scope: {
      username: '='
    },

    replace: true,

    link: function($scope) {
      $scope.visibleForm = null;
      $scope.showForm = function(name) {
        $scope.visibleForm = name || null;
      };

      $scope.processing = false;
      $scope.user = {
        id: $scope.username
      };

      $scope.password = {
        current: null,
        new: null,
        confirmation: null
      };

      var groupResource = camAPI.resource('group');
      groupResource.list(function(err, groups) {
        if (err) { throw err; }
        $scope.user.groups = groups;
      });


      var userResource = camAPI.resource('user');
      userResource.profile({
        id: $scope.user.id
      }, function(err, data) {
        angular.extend($scope.user, data);
        $scope.$root.userFullName = data.firstName + ' ' + data.lastName;
      });

      $scope.submitProfile = function() {
        $scope.processing = true;
        userResource.updateProfile($scope.user, function(err) {
          $scope.processing = false;

          if (!err) {
            $scope.userProfile.$setPristine();

            Notifications.addMessage({
              status: 'Changes saved',
              message: '',
              http: true,
              exclusive: [ 'http' ],
              duration: 5000
            });

            $scope.showForm();
          }
          else {
            Notifications.addMessage({
              status: 'Error while saving',
              message: err.message,
              http: true,
              exclusive: [ 'http' ],
              duration: 5000
            });
          }
        });
      };

      $scope.submitPassword = function() {
        $scope.processing = true;

        userResource.updateCredentials({
          id: $scope.user.id,
          password: $scope.password.confirmation,
          authenticatedUserPassword: $scope.password.current
        }, function(err) {
          $scope.processing = false;

          if (!err) {
            $scope.changePassword.$setPristine();
            $scope.password = {
              current: null,
              new: null,
              confirmation: null
            };

            Notifications.addMessage({
              status: 'Password changed',
              message: '',
              http: true,
              exclusive: [ 'http' ],
              duration: 5000
            });

            $scope.showForm();
          }
          else {
            Notifications.addMessage({
              status: 'Error while changing password',
              message: err.message,
              http: true,
              exclusive: [ 'http' ],
              duration: 5000
            });
          }
        });
      };
    }
  };
}];