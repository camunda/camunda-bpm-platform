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

const fs = require('fs');
const angular = require('angular');
const modalDialog = require('./time-to-live-dialog');

const template = require('./time-to-live.html')();

module.exports = [
  '$translate',
  '$uibModal',
  function($translate, $modal) {
    return {
      restrict: 'A',
      template: template,
      scope: {
        definition: '=timeToLive',
        customOnChange: '=onChange',
        resource: '@'
      },
      link: function($scope) {
        $scope.format = function(property) {
          if (property === 1) {
            return $translate.instant('TIME_TO_LIVE_DAY', {ttl: property});
          }

          return $translate.instant('TIME_TO_LIVE_DAYS', {ttl: property});
        };

        $scope.showDialog = () => {
          const dialog = $modal.open({
            scope: $scope,
            resolve: {},
            controller: modalDialog.controller,
            template: modalDialog.template
          });
          dialog.result.then(angular.noop).catch(console.error);
        };
      }
    };
  }
];
