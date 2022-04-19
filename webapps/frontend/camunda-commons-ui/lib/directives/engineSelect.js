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

var angular = require('../../../camunda-bpm-sdk-js/vendor/angular');
var template = require('./engineSelect.html')();

var $ = require('jquery');

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
            status: $translate.instant(
              'DIRECTIVE_ENGINE_SELECT_STATUS_NOT_FOUND'
            ),
            message: $translate.instant(
              'DIRECTIVE_ENGINE_SELECT_MESSAGE_NOT_FOUND'
            ),
            scope: $scope
          });
          $location.path('/dashboard');
        }
      })
      .catch(angular.noop);
  }
];

module.exports = function() {
  return {
    template: template,
    replace: true,
    controller: ProcessEngineSelectionController,
    link: function(scope, element, attrs) {
      var divider;

      scope.$watch(attrs.ngShow, function(newValue) {
        if (newValue && !divider) {
          divider = $('<li class="divider-vertical"></li>').insertAfter(
            element
          );
        }

        if (!newValue && divider) {
          divider.remove();
          divider = null;
        }
      });

      scope.$on('$destroy', function() {
        if (divider) {
          divider.remove();
        }
      });
    }
  };
};
