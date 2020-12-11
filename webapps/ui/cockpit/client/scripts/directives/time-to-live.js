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

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/time-to-live.html', 'utf8');

module.exports = [
  'camAPI',
  '$window',
  'Notifications',
  '$translate',
  function(camAPI, $window, Notifications, $translate) {
    return {
      restrict: 'A',
      template: template,
      scope: {
        definition: '=timeToLive',
        customOnChange: '=onChange',
        resource: '@'
      },
      link: function($scope) {
        var lastValue = getAndCorrectTimeToLiveValue();
        var resource = camAPI.resource($scope.resource);

        function customOnChange() {
          if (typeof $scope.customOnChange === 'function') {
            $scope.customOnChange();
          }
        }

        $scope.onChange = function() {
          $window.setTimeout(function() {
            var timeToLive = getAndCorrectTimeToLiveValue();

            updateValue(timeToLive)
              .then(customOnChange)
              .catch(function() {});
          });
        };

        $scope.onRemove = function() {
          updateValue(null)
            .then(function() {
              lastValue = null;
              $scope.definition.historyTimeToLive = null;
              customOnChange();
            })
            .catch(function() {});
        };

        $scope.format = function(property) {
          if (property === 1) {
            return $translate.instant('TIME_TO_LIVE_DAY', {ttl: property});
          }

          return $translate.instant('TIME_TO_LIVE_DAYS', {ttl: property});
        };

        function updateValue(timeToLive) {
          return resource
            .updateHistoryTimeToLive($scope.definition.id, {
              historyTimeToLive: timeToLive
            })
            .catch(function(error) {
              $scope.definition.historyTimeToLive = lastValue;

              Notifications.addError({
                status: $translate.instant('TIME_TO_LIVE_MESSAGE_ERR'),
                message: error
              });
            })
            .then(function() {
              lastValue = getAndCorrectTimeToLiveValue();
            })
            .catch(function() {});
        }

        function getAndCorrectTimeToLiveValue() {
          if ($scope.definition.historyTimeToLive === null) {
            return null;
          }

          return +$scope.definition.historyTimeToLive;
        }
      }
    };
  }
];
