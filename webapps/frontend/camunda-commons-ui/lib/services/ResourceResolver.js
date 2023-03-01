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
// depends on 'cam.commons.util Notifications'

module.exports = [
  '$route',
  '$q',
  '$location',
  'Notifications',
  '$translate',
  function($route, $q, $location, Notifications, $translate) {
    function getByRouteParam(paramName, options) {
      var deferred = $q.defer();

      var id = $route.current.params[paramName],
        resolve = options.resolve,
        resourceName = options.name || 'entity';

      function succeed(result) {
        deferred.resolve(result);
      }

      function fail(errorResponse) {
        var message, replace, redirectTo;
        var preventRedirection = false;

        if (errorResponse.status === 404) {
          message = $translate.instant(
            'SERVICES_RESOURCE_RESOLVER_ID_NOT_FOUND',
            {resourceName: resourceName, id: id}
          );
          replace = true;

          redirectTo = options.redirectTo || '/dashboard';

          if (typeof redirectTo == 'function') {
            // the redirection should be executed
            // inside the custom implementation of
            // redirectTo()
            preventRedirection = true;
            redirectTo(errorResponse);
          }
        } else if (errorResponse.status === 401) {
          message = $translate.instant(
            'SERVICES_RESOURCE_RESOLVER_AUTH_FAILED'
          );
          redirectTo = '/login';
        } else {
          message = $translate.instant(
            'SERVICES_RESOURCE_RESOLVER_RECEIVED_STATUS',
            {status: errorResponse.status}
          );
          redirectTo = '/dashboard';
        }

        if (!preventRedirection) {
          $location.path(redirectTo);

          if (replace) {
            $location.replace();
          }

          Notifications.addError({
            status: $translate.instant(
              'SERVICES_RESOURCE_RESOLVER_DISPLAY_FAILED',
              {resourceName: resourceName}
            ),
            message: message,
            http: true,
            exclusive: ['http']
          });
        }

        deferred.reject(message);
      }

      // resolve
      var promise = resolve(id);
      if (promise.$promise && promise.$promise.then) {
        promise = promise.$promise.then(function(response) {
          succeed(response);
        }, fail);
      } else if (promise.then) {
        promise = promise.then(succeed, fail);
      } else {
        throw new Error(
          $translate.instant('SERVICES_RESOURCE_RESOLVER_NO_PROMISE')
        );
      }

      return deferred.promise;
    }

    return {
      getByRouteParam: getByRouteParam
    };
  }
];
