/* global ngDefine: false */
ngDefine('camunda.common.services.authentication', ['angular', 'jquery'], function(module, angular, $) {
  'use strict';

  /**
   * An authentication service
   * @name Authentication
   * @memberof cam.common.services
   * @type angular.service
   */
  var AuthenticationProducer = [ '$rootScope', function($rootScope) {

    var user = null;

    function clear() {
      user = null;
    }

    // function loaded() {
    //   return loaded;
    // }

    function username() {
      return user ? user.name : null;
    }

    function canAccess(app) {
      return !user || (user.authorizedApps && user.authorizedApps.indexOf(app) !== -1);
    }

    function update(data) {
      if ((!user && data) || (user && data && data.name != user.name)) {
        authentication.user = user = data;
      }
    }

    var authentication = {
      username: username,
      canAccess: canAccess,
      clear: clear,
      update: update,
      user: user
    };

    // register with root scope
    $rootScope.authentication = authentication;

    return authentication;
  }];

  /**
   * An authentication provider
   * @name AuthenticationService
   * @memberof cam.common.services
   * @type angular.provider
   */
  var AuthenticationServiceProvider = function() {

    this.requireAuthenticatedUser = [ 'AuthenticationService', function(AuthenticationService) {
      return AuthenticationService.requireAuthenticatedUser();
    }];

    this.$get = [ '$rootScope', '$q', '$http', '$location', '$window', 'Authentication', 'Notifications', 'Uri',
          function($rootScope, $q, $http, $location, $window, Authentication, Notifications, Uri) {

      var promise = null;

      $rootScope.$on('$routeChangeStart', function() {
        promise = null;
      });

      function parseAuthentication(response) {
        var data = response.data;

        if (response.status !== 200) {
          return null;
        }

        return {
          name: data.userId,
          authorizedApps: data.authorizedApps
        };
      }

      function login(username, password) {
        var data = { 'username': username, 'password': password };
        var form = $.param(data);
        var promise = $http({
          method: 'POST',
          url: Uri.appUri('admin://auth/user/:engine/login/:appName'),
          data: form,
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }).then(parseAuthentication);

        return promise.then(function(user) {
          if (user) {
            Authentication.update(user);
          }

          return user !== null;
        }, function() {
          return false;
        });
      }

      function logout() {
        var promise = $http.post(Uri.appUri('admin://auth/user/:engine/logout'));
        return promise.then(function() {
          Authentication.clear();

          return true;
        });
      }

      function getCurrentUser() {

        if (Authentication.user) {
          return Authentication.user.name;
        } else {
          if (!promise) {
            promise = $http
              .get(Uri.appUri('admin://auth/user/:engine'))
                .then(parseAuthentication)
                .then(function(user) {
                  Authentication.update(user);
                  return user.name;
                });
          }

          return promise;
        }
      }

      function addError(error) {
        error.http = true;
        error.exclusive = [ 'http' ];

        Notifications.addError(error);
      }

      function requireAuthenticatedUser() {
        return authenticatedUser().then(function(username) {
          if (username) {
            return username;
          }

          addError({ status: 'Unauthorized', message: 'Login is required to access the resource' });
          // store current URL to come back directly after re-login?
          $location.path('/login');

          return $q.reject(new Error('no user'));
        });
      }

      function authenticatedUser() {
        var promise = $q.when(getCurrentUser());

        return promise.then(function(username) {
          return username;
        }, function() {
          return null;
        });
      }

      return {
        login: login,
        logout: logout,
        authenticatedUser: authenticatedUser,
        requireAuthenticatedUser: requireAuthenticatedUser
      };
    }];
  };

  module
    .service('Authentication', AuthenticationProducer)
    .provider('AuthenticationService', AuthenticationServiceProvider)
  ;
});
