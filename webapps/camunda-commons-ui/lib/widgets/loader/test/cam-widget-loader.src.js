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

var angular = require('../../../../../camunda-bpm-sdk-js/vendor/angular'),
    loaderDefinition = require('../cam-widget-loader');

require('../../../../vendor/ui-bootstrap-tpls-2.5.0-camunda');

var loaderModule = angular.module('loaderModule', ['ui.bootstrap']);
loaderModule.directive('camWidgetLoader', loaderDefinition);

var testModule = angular.module('testModule', [loaderModule.name]);
testModule.controller('testInteractiveController', [
  '$scope',
  '$timeout',
  function(
    $scope,
    $timeout
  ) {
    $scope.ctrlState = 'INITIAL';
    $scope.timeoutPromise = null;

    $scope.reload = function(simulateEmpty) {
      $scope.ctrlState = 'LOADING';

      $scope.timeoutPromise = $timeout(function() {
        $scope.ctrlVar1 = 'Control variable';
        $scope.ctrlState = simulateEmpty ? 'EMPTY' : 'LOADED';
      }, 1000);
    };

    $scope.fail = function() {
      $scope.ctrlState = 'ERROR';
      $scope.ctrlError = 'Something wen really wrong';

      if ($scope.timeoutPromise) {
        $timeout.cancel($scope.timeoutPromise);
      }
    };
  }]);

angular.element(document).ready(function() {
  angular.bootstrap(document.body, [testModule.name]);
});
