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

var angular = require('camunda-bpm-sdk-js/vendor/angular');
var CamSDK = require('camunda-bpm-sdk-js/lib/angularjs/index');

module.exports = [
  '$rootScope',
  '$timeout',
  '$q',
  '$cookies',
  'configuration',
  'ifUnauthorizedForwardToWelcomeApp',
  function(
    $rootScope,
    $timeout,
    $q,
    $cookies,
    configuration,
    ifUnauthorizedForwardToWelcomeApp
  ) {
    function setHeaders(options) {
      var headers = (options.headers = options.headers || {});
      var token = $cookies.get(configuration.getCsrfCookieName());

      if (token) {
        headers['X-XSRF-TOKEN'] = token;
      }
    }

    function AngularClient(config) {
      this._wrapped = new CamSDK.Client.HttpClient(config);
    }

    angular.forEach(
      ['post', 'get', 'load', 'put', 'del', 'options', 'head'],
      function(name) {
        AngularClient.prototype[name] = function(path, options) {
          var myTimeout = $timeout(function() {}, 100000);

          setHeaders(options);

          var original = angular.isFunction(options.done)
            ? options.done
            : angular.noop;

          options.done = function(err, result, headers) {
            function applyResponse() {
              ifUnauthorizedForwardToWelcomeApp(headers);

              // in case the session expired
              if (err && err.status === 401) {
                // broadcast that the authentication changed
                $rootScope.$broadcast('authentication.changed', null);
                // set authentication to null
                $rootScope.authentication = null;
                // broadcast event that a login is required
                // proceeds a redirect to /login
                $rootScope.$broadcast('authentication.login.required');
                return;
              }

              original(err, result);
            }

            var phase = $rootScope.$$phase;

            if (phase !== '$apply' && phase !== '$digest') {
              $rootScope.$apply(applyResponse);
            } else {
              applyResponse();
            }
            $timeout.cancel(myTimeout);
          };

          return $q.when(this._wrapped[name](path, options));
        };
      }
    );

    angular.forEach(['on', 'once', 'off', 'trigger'], function(name) {
      AngularClient.prototype[name] = function() {
        this._wrapped[name].apply(this, arguments);
      };
    });

    return AngularClient;
  }
];
