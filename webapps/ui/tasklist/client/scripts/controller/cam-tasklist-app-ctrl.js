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

var TasklistApp = (function() {
  function TasklistApp() {
    this.refreshProvider = null;
  }

  return TasklistApp;
})();

module.exports = [
  'camAPI',
  'configuration',
  '$window',
  '$interval',
  '$scope',
  function(camAPI, configuration, $window, $interval, $scope) {
    // create a new tasklistApp
    $scope.tasklistApp = new TasklistApp();
    $scope.appVendor = configuration.getAppVendor();
    $scope.appName = configuration.getAppName();

    // doing so, there's no `{{ appVendor }} {{ appName }}`
    // visible in the title tag as the app loads
    var htmlTitle = document.querySelector('head > title');
    htmlTitle.textContent = $scope.appVendor + ' ' + $scope.appName;

    function getUserProfile(auth) {
      if (!auth || !auth.name) {
        $scope.userFullName = null;
        return;
      }

      var userService = camAPI.resource('user');
      userService.profile(auth.name, function(err, info) {
        if (err) {
          $scope.userFullName = null;
          throw err;
        }
        $scope.userFullName = info.firstName + ' ' + info.lastName;
      });
    }

    $scope.$on('authentication.changed', function(ev, auth) {
      getUserProfile(auth);
    });

    getUserProfile($scope.authentication);

    // app wide refresh event triggering
    var refreshInterval = $interval(function() {
      $scope.$root.$broadcast('refresh');
    }, 10000);

    $scope.$on('$destroy', function() {
      $interval.cancel(refreshInterval);
    });
  }
];
