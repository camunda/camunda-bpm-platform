'use strict';
/* jshint browserify: true */
var fs = require('fs');
var template = fs.readFileSync(__dirname + '/user-profile.html', 'utf8');
var angular = require('camunda-commons-ui/vendor/angular');

module.exports = ['camAPI', 'Notifications', 'translateFilter', function(camAPI, Notifications, translateFilter) {
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
              status: translateFilter('CHANGES_SAVED'),
              message: '',
              http: true,
              exclusive: [ 'http' ],
              duration: 5000
            });

            $scope.showForm();
          }
          else {
            Notifications.addMessage({
              status: translateFilter('ERROR_WHILE_SAVING'),
              message: err.message,
              http: true,
              exclusive: [ 'http' ],
              duration: 5000
            });
          }
        });
      };

      function checkPassword() {
        $scope.passwordsMismatch = $scope.changePassword.new.$dirty &&
                                      $scope.changePassword.confirmation.$dirty &&
                                      $scope.password.new !== $scope.password.confirmation;

        $scope.changePassword.new.$setValidity('mismatch', !$scope.passwordsMismatch);
        $scope.changePassword.confirmation.$setValidity('mismatch', !$scope.passwordsMismatch);
      }

      $scope.$watch('password.new', checkPassword);
      $scope.$watch('password.confirmation', checkPassword);
      $scope.$watch('changePassword.new.$dirty', checkPassword);
      $scope.$watch('changePassword.confirmation.$dirty', checkPassword);


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
              status: translateFilter('PASSWORD_CHANGED'),
              message: '',
              http: true,
              exclusive: [ 'http' ],
              duration: 5000
            });

            $scope.showForm();
          }
          else {
            Notifications.addMessage({
              status: translateFilter('ERROR_WHILE_CHANGING_PASSWORD'),
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
