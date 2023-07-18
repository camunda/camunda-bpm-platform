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

/**
 * @namespace cam.common.services
 */

'use strict';

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  util = require('./../util/index'),
  escape = require('./escape'),
  debounce = require('./debounce'),
  RequestLogger = require('./RequestLogger'),
  ResourceResolver = require('./ResourceResolver'),
  HttpClient = require('./HttpClient'),
  unescape = require('./unescape'),
  fixDate = require('./fixDate'),
  ifUnauthorizedForwardToWelcomeApp = require('./ifUnauthorizedForwardToWelcomeApp'),
  unfixDate = require('./unfixDate'),
  shouldDisplayAuthenticationError = require('./shouldDisplayAuthenticationError'),
  canonicalAppName = require('./canocialAppName');

var ngModule = angular.module('camunda.common.services', [
  // `ResourceResolver` relies on cam.commons.util for Notifications
  util.name
]);

ngModule.filter('escape', escape);

ngModule.factory('debounce', debounce);
ngModule.factory('RequestLogger', RequestLogger);
ngModule.factory('ResourceResolver', ResourceResolver);
ngModule.factory('camAPIHttpClient', HttpClient);
ngModule.factory('unescape', unescape);
ngModule.factory('fixDate', fixDate);
ngModule.factory(
  'ifUnauthorizedForwardToWelcomeApp',
  ifUnauthorizedForwardToWelcomeApp
);
ngModule.factory('unfixDate', unfixDate);
ngModule.factory(
  'shouldDisplayAuthenticationError',
  shouldDisplayAuthenticationError
);
ngModule.provider('canonicalAppName', canonicalAppName);

/**
 * Register http status interceptor per default
 */
ngModule.config([
  '$httpProvider',
  function($httpProvider) {
    $httpProvider.interceptors.push([
      '$rootScope',
      '$q',
      'RequestLogger',
      'ifUnauthorizedForwardToWelcomeApp',
      function(
        $rootScope,
        $q,
        RequestLogger,
        ifUnauthorizedForwardToWelcomeApp
      ) {
        RequestLogger.logStarted();

        return {
          response: function(response) {
            RequestLogger.logFinished();
            ifUnauthorizedForwardToWelcomeApp(response.headers());

            return response;
          },
          responseError: function(response) {
            RequestLogger.logFinished();

            var httpError = {
              status: parseInt(response.status),
              response: response,
              data: response.data
            };

            $rootScope.$broadcast('httpError', httpError);

            return $q.reject(response);
          }
        };
      }
    ]);
  }
]);

ngModule.config([
  '$httpProvider',
  '$windowProvider',
  function($httpProvider, $windowProvider) {
    // eslint-disable-next-line
    if (!DEV_MODE) {
      var window = $windowProvider.$get();
      var uri = window.location.href;

      var match = uri.match(/\/(?:app)(?!.*\/app\/)\/([\w-]+)\/([\w-]+)/);
      if (match) {
        $httpProvider.defaults.headers.get = {'X-Authorized-Engine': match[2]};
      } else {
        throw new Error('no process engine selected');
      }
    } else {
      $httpProvider.defaults.headers.get = {'X-Authorized-Engine': 'default'};
    }
  }
]);

module.exports = ngModule;
