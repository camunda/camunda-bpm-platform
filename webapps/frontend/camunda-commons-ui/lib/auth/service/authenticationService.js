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

require('camunda-bpm-sdk-js/vendor/angular');
var $ = require('jquery');
var Authentication = require('../util/authentication');

/**
 * An authentication provider
 * @name AuthenticationService
 * @memberof cam.common.auth.service
 * @type angular.service
 */
module.exports = [
  '$rootScope',
  '$q',
  '$http',
  'Uri',
  '$route',
  function($rootScope, $q, $http, Uri, $route) {
    function emit(event, a, b) {
      $rootScope.$broadcast(event, a, b);
    }

    function parse(response) {
      if (response.status !== 200) {
        return $q.reject(response);
      }

      var data = response.data;

      return new Authentication({
        name: data.userId,
        authorizedApps: data.authorizedApps
      });
    }

    function update(authentication) {
      $rootScope.authentication = authentication;
      emit('authentication.changed', authentication);
    }

    /**
     * Force an update of the current application authentication
     *
     * @method AuthenticationService#update
     *
     * @param {Authentication} authentication
     */
    this.updateAuthentication = update;

    //////// login logic ///////////////

    /**
     * Login into the application with the given credentials
     *
     * @method AuthenticationService#login
     *
     * @param  {String} username
     * @param  {String} password
     *
     * @return {Promise<Authentication, Response>} the promise with the new authentication or failure response
     */
    this.login = function(username, password) {
      var form = $.param({
        username: username,
        password: password
      });

      function success(authentication) {
        // When the next subsequent request after the location change is
        // a modifying request (POST), no valid CSRF token is available
        $http.get(Uri.appUri('engine://engine/')).then(function() {
          update(authentication);
          emit('authentication.login.success', authentication);
        });

        return authentication;
      }

      function error(response) {
        emit('authentication.login.failure', response);
        return $q.reject(response);
      }

      function performRequest() {
        return $http({
          method: 'POST',
          url: Uri.appUri('admin://auth/user/:engine/login/:appName'),
          data: form,
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
          }
        });
      }

      // We have to perform a GET request before login to make sure we have an up to date CSRF-Cookie
      return $http
        .get(Uri.appUri('engine://engine/'))
        .then(performRequest)
        .then(parse)
        .then(success, error);
    };

    //////// logout logic ///////////////

    /**
     * Logout from the application.
     *
     * @method AuthenticationService#logout
     *
     * @return {Promise<Void, Response>} a promise that resolves to the failed response on error
     */
    this.logout = function(engine) {
      function success(response) {
        update(null);
        emit('authentication.logout.success', response);
      }

      function error(response) {
        emit('authentication.logout.failure', response);
        return $q.reject(response);
      }

      return $http
        .post(
          Uri.appUri('admin://auth/user/' + (engine || ':engine') + '/logout')
        )
        .then(success, error);
    };

    //////// get current auth ///////////////

    /**
     * A promise that returns the current authentication, either fetched from server
     * or from local application context. Will be reset when the authentication changed only.
     */
    var authenticationPromise;

    $rootScope.$on('authentication.changed', function(e, authentication) {
      authenticationPromise = $q[authentication ? 'when' : 'reject'](
        authentication
      );

      if (!authentication) {
        $route.reload();
      }
    });

    /**
     * Resolves the current authentication, either fetched from server or locally.
     *
     * @method AuthenticationService#getAuthentication
     *
     * @return {Promise<Authentication, Response>} a promise resolving to the current users authentication
     */
    this.getAuthentication = function() {
      function success(authentication) {
        update(authentication);
        return authentication;
      }

      if (!authenticationPromise) {
        // if already authenticated, return stored credentials
        if ($rootScope.authentication) {
          authenticationPromise = $q.when($rootScope.authentication);
        } else {
          authenticationPromise = $http
            .get(Uri.appUri('admin://auth/user/:engine'))
            .then(parse)
            .then(success);
        }
      }

      return authenticationPromise;
    };

    //////// secure route integration ///////////////

    $rootScope.$on('$routeChangeStart', function(event, next) {
      // secures routes that are annotated with
      //
      // {
      //   authentication: 'required'
      // }

      if (next.authentication) {
        if (!next.resolve) {
          next.resolve = {};
        }

        if (!next.resolve.authentication) {
          next.resolve.authentication = [
            'AuthenticationService',
            function(AuthenticationService) {
              return AuthenticationService.getAuthentication().catch(function(
                response
              ) {
                if (next.authentication === 'optional') {
                  return null;
                } else {
                  emit('authentication.login.required', next);
                  return $q.reject(response);
                }
              });
            }
          ];
        }
      }
    });
  }
];
