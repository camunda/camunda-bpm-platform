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

var SearchFactory = [
  '$location',
  '$rootScope',
  function($location, $rootScope) {
    var silent = false;

    $rootScope.$on('$routeUpdate', function(e, lastRoute) {
      if (silent) {
        silent = false;
      } else {
        $rootScope.$broadcast('$routeChanged', lastRoute);
      }
    });

    $rootScope.$on('$routeChangeSuccess', function() {
      silent = false;
    });

    var search = function() {
      return $location.search.apply($location, arguments);
    };

    search.updateSilently = function(params, replaceFlag) {
      var oldPath = $location.absUrl();

      angular.forEach(params, function(value, key) {
        $location.search(key, value);
      });

      var newPath = $location.absUrl();

      if (newPath != oldPath) {
        silent = true;
      }

      if (replaceFlag) {
        $location.replace();
      }
    };

    return search;
  }
];

var searchModule = angular.module('camunda.common.search', []);

searchModule.factory('search', SearchFactory);

module.exports = searchModule;
