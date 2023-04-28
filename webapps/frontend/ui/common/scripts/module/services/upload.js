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

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$window',
  '$q',
  '$rootScope',
  '$cookies',
  '$http',
  function($window, $q, $rootScope, $cookies, $http) {
    return function(url, files, fields) {
      var deferred = $q.defer();

      if (!angular.isArray(files)) {
        files = [files];
      }

      fields = fields || {};

      var segments = files.map(function(entry, index) {
        return (
          'Content-Disposition: form-data; name="data' +
          index +
          '"; filename="' +
          entry.file.name +
          '"\r\nContent-Type: text/xml\r\n\r\n' +
          entry.content +
          '\r\n'
        );
      });

      segments = segments.concat(
        Object.keys(fields).map(function(name) {
          var value = fields[name];

          return (
            'Content-Disposition: form-data; name="' +
            name +
            '"\r\n\r\n' +
            value +
            '\r\n'
          );
        })
      );

      var sBoundary = '---------------------------' + Date.now().toString(16);

      var sData =
        '--' +
        sBoundary +
        '\r\n' +
        segments.join('--' + sBoundary + '\r\n') +
        '--' +
        sBoundary +
        '--\r\n';

      $http
        .post(url, sData, {
          transformRequest: angular.identity,
          headers: {
            'Content-Type': 'multipart/form-data; boundary=' + sBoundary
          }
        })
        .then(function(res) {
          deferred.resolve(res.data);
        })
        .catch(function(err) {
          if (err.status === 401) {
            // broadcast that the authentication changed
            $rootScope.$broadcast('authentication.changed', null);
            // set authentication to null
            $rootScope.authentication = null;
            // broadcast event that a login is required
            // proceeds a redirect to /login
            $rootScope.$broadcast('authentication.login.required');
          }
          deferred.reject(err);
        });

      return deferred.promise;
    };
  }
];
