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

module.exports = [
  '$rootScope',
  function($rootScope) {
    var activeLoaders = [];
    var eventName = 'LoaderService:active-loaders-changed';

    return {
      startLoading: startLoading,
      addStatusListener: addStatusListener
    };

    /**
     * Starts new loading and return function that stop loading when called.
     *
     * @returns {stopLoading}
     */
    function startLoading() {
      var stopLoading = function() {
        activeLoaders = activeLoaders.filter(function(loader) {
          return loader !== stopLoading;
        });
        fireChanges();
      };

      activeLoaders.push(stopLoading);
      fireChanges();

      return stopLoading;
    }

    function fireChanges() {
      $rootScope.$broadcast(eventName);
    }

    /**
     * Adds new status listener, that will be called when loading status changes.
     * Returns function that removes listener.
     * Listeners uses $scope to listen to LoaderService:active-loaders-changed, so there is no need to manually
     * remove listener when $scope is destroyed.
     *
     * @param $scope
     * @param callback
     * @returns {*}
     */
    function addStatusListener($scope, callback) {
      callback(getLoadingStatus());

      return $scope.$on(eventName, function() {
        var status = getLoadingStatus();

        callback(status);
      });
    }

    function getLoadingStatus() {
      return activeLoaders.length === 0 ? 'LOADED' : 'LOADING';
    }
  }
];
