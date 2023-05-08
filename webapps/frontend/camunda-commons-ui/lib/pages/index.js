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
require('angular-route');

var $ = require('jquery');

var pagesModule = angular.module('camunda.common.pages', ['ngRoute']);

var ResponseErrorHandlerInitializer = [
  '$rootScope',
  '$location',
  'Notifications',
  'AuthenticationService',
  'shouldDisplayAuthenticationError',
  '$translate',
  'configuration',
  function(
    $rootScope,
    $location,
    Notifications,
    AuthenticationService,
    shouldDisplayAuthenticationError,
    $translate,
    configuration
  ) {
    function addError(error) {
      error.http = true;
      error.exclusive = ['http'];

      Notifications.addError(error);
    }

    function setHeadTitle() {
      var pageTitle =
        configuration.getAppVendor() + ' ' + configuration.getAppName();

      $('head title').text(pageTitle);
    }

    /**
     * A handler function that handles HTTP error responses,
     * i.e. 4XX and 5XX responses by redirecting / notifying the user.
     */
    function handleHttpError(event, error) {
      var status = error.status,
        data = error.data;

      // avoid displaying any error message when performing a request against /camunda-welcome
      // since the get request against it is only necessary to determine if deployed
      const config = error.response.config;
      if (config.method === 'GET' && config.url === '/camunda-welcome') {
        return;
      }

      switch (status) {
        case 500:
          if (data && data.message) {
            addError({
              status: $translate.instant('PAGES_STATUS_SERVER_ERROR'),
              message: data.message,
              exceptionType: data.exceptionType
            });
          } else {
            addError({
              status: $translate.instant('PAGES_STATUS_SERVER_ERROR'),
              message: $translate.instant('PAGES_MSG_SERVER_ERROR')
            });
          }
          break;

        case 0:
          addError({
            status: $translate.instant('PAGES_STATUS_REQUEST_TIMEOUT'),
            message: $translate.instant('PAGES_MSG_REQUEST_TIMEOUT')
          });
          break;

        case 401:
          if ($location.absUrl().indexOf('/setup/#') !== -1) {
            $location.path('/setup');
          } else {
            setHeadTitle($location.absUrl());

            AuthenticationService.updateAuthentication(null);
            $rootScope.$broadcast('authentication.login.required');
          }
          break;

        case 403:
          if (data.type == 'AuthorizationException') {
            var message;
            if (data.resourceId) {
              message = $translate.instant(
                'PAGES_MSG_ACCESS_DENIED_RESOURCE_ID',
                {
                  permissionName: data.permissionName.toLowerCase(),
                  resourceName: data.resourceName.toLowerCase(),
                  resourceId: data.resourceId
                }
              );
            } else {
              var missingAuths = data.missingAuthorizations.map(function(
                missingAuth
              ) {
                return (
                  "'" +
                  missingAuth.permissionName +
                  "'" +
                  ' ' +
                  missingAuth.resourceName +
                  's'
                );
              });

              message = $translate.instant('PAGES_MSG_ACCESS_DENIED', {
                missingAuths: missingAuths.join()
              });
            }

            addError({
              status: $translate.instant('PAGES_STATUS_ACCESS_DENIED'),
              message: message
            });
          } else {
            addError({
              status: $translate.instant('PAGES_STATUS_ACCESS_DENIED'),
              message: $translate.instant('PAGES_MSG_ACTION_DENIED')
            });
          }
          break;

        case 404:
          if (shouldDisplayAuthenticationError()) {
            addError({
              status: $translate.instant('PAGES_STATUS_NOT_FOUND'),
              message: $translate.instant('PAGES_MSG_NOT_FOUND')
            });
          }
          break;
        default:
          addError({
            status: $translate.instant('PAGES_STATUS_COMMUNICATION_ERROR'),
            message: $translate.instant('PAGES_MSG_COMMUNICATION_ERROR', {
              status: status
            })
          });
      }
    }

    // triggered by httpStatusInterceptor
    $rootScope.$on('httpError', handleHttpError);
  }
];

var ProcessEngineSelectionController = [
  '$scope',
  '$http',
  '$location',
  '$window',
  'Uri',
  'Notifications',
  '$translate',
  function($scope, $http, $location, $window, Uri, Notifications, $translate) {
    var current = Uri.appUri(':engine');
    var enginesByName = {};

    $http
      .get(Uri.appUri('engine://engine/'))
      .then(function(response) {
        $scope.engines = response.data;

        angular.forEach($scope.engines, function(engine) {
          enginesByName[engine.name] = engine;
        });

        $scope.currentEngine = enginesByName[current];

        if (!$scope.currentEngine) {
          Notifications.addError({
            status: $translate.instant('PAGES_STATUS_NOT_FOUND'),
            message: $translate.instant('PAGES_MSG_ENGINE_NOT_EXISTS'),
            scope: $scope
          });
          $location.path('/');
        }
      })
      .catch(angular.noop);

    $scope.$watch('currentEngine', function(engine) {
      if (engine && current !== engine.name) {
        $window.location.href = Uri.appUri('app://../' + engine.name + '/');
      }
    });
  }
];

var NavigationController = [
  '$scope',
  '$location',
  function($scope, $location) {
    $scope.activeClass = function(link) {
      var path = $location.absUrl();
      return path.indexOf(link) != -1 ? 'active' : '';
    };
  }
];

var AuthenticationController = [
  '$scope',
  '$window',
  '$cacheFactory',
  '$location',
  'Notifications',
  'AuthenticationService',
  function(
    $scope,
    $window,
    $cacheFactory,
    $location,
    Notifications,
    AuthenticationService
  ) {
    $scope.logout = function() {
      AuthenticationService.logout();
    };
  }
];

module.exports = pagesModule
  .run(ResponseErrorHandlerInitializer)
  .controller(
    'ProcessEngineSelectionController',
    ProcessEngineSelectionController
  )
  .controller('AuthenticationController', AuthenticationController)
  .controller('NavigationController', NavigationController);
