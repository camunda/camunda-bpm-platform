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
  '$scope', 'decisionList', 'Views', //'localConf', '$translate',
  function($scope, decisionList, Views /*, localConf, $translate*/) {
    $scope.loadingState = 'LOADING';
    $scope.drdDashboard = Views.getProvider({ component: 'cockpit.plugin.drd.dashboard' });
    $scope.isDrdDashboardAvailable = !!$scope.drdDashboard;

    decisionList
      .getDecisionsLists()
      .then(function(data) {
        $scope.loadingState = 'LOADED';

        $scope.decisionCount = data.decisions.length;
        $scope.decisions = data.decisions;

        $scope.drdsCount = data.drds.length;
        $scope.drds = data.drds;
      })
      .catch(function(err) {
        $scope.loadingError = err.message;
        $scope.loadingState = 'ERROR';

        throw err;
      });

    $scope.drdDashboardVars = { read: [ 'drdsCount', 'drds'] };
  }
];
